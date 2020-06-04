package de.finnik.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import de.finnik.AES.AES;
import de.finnik.gui.Animation;
import de.finnik.gui.PassFrame;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.Password;
import de.finnik.passvault.PasswordGenerator;
import de.finnik.passvault.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.finnik.gui.Var.*;

public class PassDrive {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    public static Drive DRIVE;
    private static boolean stopAnimation;
    private static final Runnable compare = () -> {
        try {
            INACTIVITY_LISTENER.stop();
            if (DRIVE == null)
                initialize();
            FileList appDataFolder = DRIVE.files().list().setQ("name = 'pass'").setSpaces("appDataFolder").execute();
            // Checks whether pass file exists on drive
            if (appDataFolder.getFiles().size() > 0) {
                // Checks whether DRIVE_PASSWORD property is not set
                if (PassProperty.DRIVE_PASSWORD.getValue().length() == 0) {
                    DIALOG.input(FRAME, LANG.getProperty("drive.jop.enterDrivePass"), pass -> {
                        try {
                            Password.readPasswords(DRIVE.files().get(appDataFolder.getFiles().get(0).getId()).executeMediaAsInputStream(), pass);
                            PassProperty.DRIVE_PASSWORD.setValue(new AES(PassFrame.password).encrypt(pass));
                            compare();
                        } catch (AES.WrongPasswordException e) {
                            // Lets the user delete drive passwords if password is forgotten
                            DIALOG.confirm(FRAME, LANG.getProperty("drive.jop.wrongDrivePass"), b -> {
                                if (b) {
                                    DIALOG.input(FRAME, LANG.getProperty("drive.jop.deleteAllDrivePasswords"), string -> {
                                        if (string.replace("'", "").equals(LANG.getProperty("drive.jop.deleteAllDrivePasswords").split("'")[1])) {
                                            appDataFolder.getFiles().forEach(file -> {
                                                try {
                                                    DRIVE.files().delete(file.getId()).execute();
                                                    compare();
                                                } catch (IOException ioException) {
                                                    LOG.error("Error while deleting pass file {}", file.getId(), ioException);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, true);
                    return;
                }
                LOG.info("Synchronizing...");
                String drivePass;
                List<Password> drivePasswords;
                try {
                    drivePass = new AES(PassFrame.password).decrypt(PassProperty.DRIVE_PASSWORD.getValue());
                    drivePasswords = Password.readPasswords(DRIVE.files().get(appDataFolder.getFiles().get(0).getId()).executeMediaAsInputStream(), drivePass);
                } catch (AES.WrongPasswordException e) {
                    // Stored DRIVE_PASSWORD is incorrect -> Reset it and restart compare function
                    PassProperty.DRIVE_PASSWORD.setValue("");
                    compare();
                    return;
                }
                List<Password> compared = CompareVaults.compare(PassFrame.passwordList,
                        drivePasswords);
                Arrays.stream(CompareVaults.changeLog(PassFrame.passwordList, compared)).forEach(s -> LOG.info("Synchronization: " + String.format(s, "Drive", "Local")));
                Arrays.stream(CompareVaults.changeLog(drivePasswords, compared)).forEach(s -> LOG.info("Synchronization: " + String.format(s, "Local", "Drive")));
                PassFrame.passwordList = compared;

                // Not PassFrame.savePasswords() because it would call this function again -> endless loop
                Password.savePasswords(PassFrame.passwordList, PASSWORDS, PassFrame.password);

                java.io.File temp = java.io.File.createTempFile("pass", "vault");
                temp.deleteOnExit();
                Password.savePasswords(PassFrame.passwordList, temp, drivePass);

                File old = appDataFolder.getFiles().get(0);
                File file = new File();
                file.setName(old.getName());
                file.setMimeType("text/plain");
                DRIVE.files()
                        .update(old.getId(), file, new FileContent("text/plain", temp))
                        .execute();
                LOG.info("Successfully synchronized with Google Drive!");
                Files.delete(temp.toPath());
            } else {
                String drivePass = PasswordGenerator.generatePassword(12, PasswordGenerator.PassChars.BIG_LETTERS, PasswordGenerator.PassChars.SMALL_LETTERS, PasswordGenerator.PassChars.NUMBERS);

                java.io.File temp = java.io.File.createTempFile("pass", "vault");
                temp.deleteOnExit();
                Password.savePasswords(PassFrame.passwordList, temp, drivePass);
                File file = new File();
                file.setParents(Collections.singletonList("appDataFolder"));
                file.setName(PASSWORDS.getName());
                File newFile = DRIVE.files()
                        .create(file, new FileContent("text/plain", temp))
                        .setFields("id")
                        .execute();
                LOG.info("Created drive file with file id {}!", newFile.getId());
                Files.delete(temp.toPath());

                PassProperty.DRIVE_PASSWORD.setValue(new AES(PassFrame.password).encrypt(drivePass));
                DIALOG.message(FRAME, String.format(LANG.getProperty("drive.jop.createdDrivePass"), drivePass));
            }
            ((PassFrame) FRAME).refreshVisibility();
            INACTIVITY_LISTENER.start();
        } catch (IOException | GeneralSecurityException ioException) {
            LOG.error("Error while synchronizing with Google Drive:", ioException);
        }
    };

    public static void compare(Runnable andThen) {
        compare();
        SERVICE.execute(andThen);
    }

    public static void compare() {
        URL url = null;
        try {
            url = new URL("http://www.googleapis.com");
            url.openConnection().connect();
        } catch (UnknownHostException e) {
            DIALOG.message(FRAME, String.format(LANG.getProperty("drive.jop.noInternet"), url.getHost()));
            return;
        } catch (IOException ioException) {
            LOG.error("Connection error", ioException);
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Animation driveAnimation = ((PassFrame) FRAME).driveAnimation;
            driveAnimation.angle++;
            if (stopAnimation && driveAnimation.isFinished()) {
                scheduledExecutorService.shutdown();
                driveAnimation.stop();
            }
            COMPONENTS.get("passFrame.lbl.refresh").repaint();
        }, 0, 3, TimeUnit.MILLISECONDS);
        SERVICE.execute(compare);

        stopAnimation = false;
        SERVICE.execute(() -> stopAnimation = true);
    }

    private static void initialize() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        DRIVE = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APP_INFO.getProperty("app.name"))
                .build();
    }

    public static void restart() {
        DRIVE = null;
    }


    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = PassDrive.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList(DriveScopes.DRIVE_APPDATA))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver, url -> {
            try {
                Utils.Browser.browse(url);
            } catch (URISyntaxException | IOException e) {
                LOG.error("Error trying to open Google login page!");
            }
        }).authorize("user");
    }
}
