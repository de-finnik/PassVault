package de.finnik.drive;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.java6.auth.oauth2.*;
import com.google.api.client.extensions.jetty.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.*;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.*;
import com.google.api.client.util.store.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.*;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.*;
import de.finnik.AES.*;
import de.finnik.gui.*;
import de.finnik.passvault.*;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import static de.finnik.gui.Var.*;

public class PassDrive {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static Drive DRIVE;
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();
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
                                                } catch (IOException ioException) {
                                                    LOG.error("Error while deleting pass file {}", file.getId(), ioException);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            DIALOG.message(FRAME, LANG.getProperty("jop.wrongPass"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, true);
                    return;
                }
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
                PassFrame.passwordList = CompareVaults.compare(PassFrame.passwordList,
                        drivePasswords);

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
                LOG.info("Synchronized with Google Drive!");
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
        SERVICE.execute(compare);
    }

    private static void initialize() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        DRIVE = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APP_INFO.getProperty("app.name"))
                .build();
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
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
