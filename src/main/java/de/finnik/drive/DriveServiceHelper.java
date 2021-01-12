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
import de.finnik.AES.AES;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.finnik.gui.Var.*;

/**
 * Creates the connection to Google Drive and handles the files stored on Google Drive
 */
public class DriveServiceHelper {
    /**
     * The name of the file where everything is stored on Google Drive
     */
    private static final String FILE_NAME = "pass";
    /**
     * The {@link Drive} object
     */
    private final Drive mDrive;

    /**
     * List of files on Google Drive in AppData folder with the name {@link DriveServiceHelper#FILE_NAME}
     * size is either 0 and not exceeding 1
     */
    private List<File> passFile;

    public DriveServiceHelper(Drive mDrive) {
        this.mDrive = mDrive;
        refreshPassFile();
    }

    /**
     * Calls {@link DriveServiceHelper#refreshPassFile()} and then checks whether {@link DriveServiceHelper#passFile}' size is bigger than 0
     *
     * @return {@link DriveServiceHelper#passFile}'s size is > 0 -> pass file exists or not
     */
    public boolean passFileExists() {
        refreshPassFile();
        return passFile.size() > 0;
    }

    /**
     * Calls {@link DriveServiceHelper#passFileId()} and then reads this file's content via the given {@link AES} object
     * and {@link Password#readPasswords(InputStream, AES)}
     *
     * @param aes The {@link AES} object containing the drive password
     * @return The read list of {@link Password} objects stored on Google Drive
     * @throws IOException Error while reading pass file
     */
    public List<Password> readPasswords(AES aes) throws IOException {
        InputStream is = mDrive.files().get(passFileId()).executeMediaAsInputStream();
        return Password.readPasswords(is, aes);
    }

    /**
     * Stores a given list of {@link Password} objects encrypted with a given {@link AES} object
     * to the user's Google Drive
     *
     * @param passwords {@link Password} objects to be stored
     * @param aes       The {@link AES} object to encrypt the passwords
     * @throws IOException Error while writing pass file
     */
    public void savePasswords(List<Password> passwords, AES aes) throws IOException {
        java.io.File tempFile = java.io.File.createTempFile("pass", "vault");
        Password.savePasswords(passwords, tempFile, aes);
        FileContent content = new FileContent("text/plain", tempFile);
        if (passFileExists()) {
            File old = mDrive.files().get(passFileId()).execute();
            File result = new File();
            result.setName(old.getName());
            result.setMimeType("text/plain");
            mDrive.files()
                    .update(old.getId(), result, content)
                    .execute();
        } else {
            File old = new File();
            old.setParents(Collections.singletonList("appDataFolder"));
            old.setName(FILE_NAME);

            mDrive.files()
                    .create(old, content)
                    .setFields("id")
                    .execute();
        }
        refreshPassFile();
    }

    /**
     * Deletes the pass file from Google Drive and then calls {@link DriveServiceHelper#refreshPassFile()}
     *
     * @throws IOException Error while deleting pass file
     * @see DriveServiceHelper#passFileId()
     */
    public void deletePassFile() throws IOException {
        mDrive.files().delete(passFileId()).execute();
        refreshPassFile();
    }

    /**
     * @return The id of the first file in {@link DriveServiceHelper#passFile}
     */
    private String passFileId() {
        return passFile.get(0).getId();
    }

    /**
     * Checks Google Drive for all files in the app data folder with file name of {@link DriveServiceHelper#FILE_NAME}
     */
    private void refreshPassFile() {
        try {
            passFile = mDrive.files().list().setQ("name = '" + FILE_NAME + "'").setSpaces("appDataFolder").execute().getFiles();
        } catch (IOException e) {
            LOG.error("Error while accessing passwords on Drive: ", e);
            passFile = new ArrayList<>();
        }
    }

    /**
     * Constructs a {@link Drive} object of PassVault's Google Drive application
     */
    public static final class Builder {
        private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

        public static Drive buildDrive() throws IOException, GeneralSecurityException {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
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
            InputStream in = DriveServiceHelper.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList(DriveScopes.DRIVE_APPDATA))
                    .setDataStoreFactory(new FileDataStoreFactory(APP_DIR))
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
}
