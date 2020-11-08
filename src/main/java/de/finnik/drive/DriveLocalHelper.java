package de.finnik.drive;

import de.finnik.AES.AES;
import de.finnik.AES.RealRandom;
import de.finnik.gui.customComponents.Animation;
import de.finnik.gui.mainFrame.PassFrame;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.passwords.PasswordGenerator;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.finnik.gui.Var.*;

/**
 * Is responsible for the cooperation between {@link DriveServiceHelper} and the local PassVault installation
 */
public class DriveLocalHelper {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    boolean stopAnimation = false;
    private DriveServiceHelper driveServiceHelper;

    /**
     * Generates a secure password for the connection to Google Drive.
     * If the given seed is positive, it'll be used, else it'll be ignored
     *
     * @param seed A random seed. If it's smaller than 0, it'll be ignored
     * @return The generated password
     */
    private static String generateDrivePass(long seed) {
        return (seed >= 0 ? new PasswordGenerator(seed) : new PasswordGenerator()).generatePassword(12, 15, PasswordGenerator.PassChars.BIG_LETTERS, PasswordGenerator.PassChars.SMALL_LETTERS, PasswordGenerator.PassChars.NUMBERS, PasswordGenerator.PassChars.SPECIAL_CHARACTERS);
    }

    /**
     * Calls {@link DriveLocalHelper#synchronize()} and appends a given runnable to the
     * task list of {@link DriveLocalHelper#executorService}
     *
     * @param andThen The runnable to be executed after the synchronisation
     */
    public void synchronize(Runnable andThen) {
        synchronize();
        executorService.execute(andThen);
    }

    /**
     * Checks for a internet connection and then starts the synchronisation.
     */
    public void synchronize() {
        // Test internet connection to google apis
        URL url = null;
        try {
            url = new URL("http://www.googleapis.com");
            url.openConnection().connect();
        } catch (Exception e) {
            assert url != null;
            DIALOG.message(FRAME, String.format(LANG.getString("drive.jop.noInternet"), url.getHost()));
            return;
        }
        Runnable run = () -> {
            try {
                // Initializes driveServiceHelper if necessary
                if (driveServiceHelper == null) {
                    INACTIVITY_LISTENER.stop();
                    driveServiceHelper = new DriveServiceHelper(DriveServiceHelper.Builder.buildDrive());
                    INACTIVITY_LISTENER.start();
                }
                startAnimation();
                // Picks up drive password
                AES drivePass = getDriveAES();
                if (drivePass == null) return;
                try {
                    // The actual sync
                    PassFrame.passwordList = compareToDriveAndSync(PassFrame.passwordList, drivePass);
                    // When this code is executed, the drive pass was correct -> it'll be stored inside PassProperty
                    if (!PassProperty.DRIVE_PASSWORD.getValue().equals(drivePass.getPass())) {
                        PassProperty.DRIVE_PASSWORD.setValue(drivePass.getPass(), PassFrame.aes);
                    }
                    LOG.info("Synchronized with Drive");
                } catch (AES.WrongPasswordException e) {
                    wrongDrivePass();
                }
                stopAnimation();
                ((PassFrame) FRAME).refreshDriveVisibility();
            } catch (Exception e) {
                LOG.error("Error while synchronizing with Drive", e);
            }
        };
        executorService.execute(run);
    }

    /**
     * Is executed when the drive pass was incorrect.
     * After the user inputted a wrong password, he is asked to delete the pass file on Google Drive.
     *
     * @throws IOException Pass file couldn't be deleted
     */
    private void wrongDrivePass() throws IOException {
        if (PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
            if (DIALOG.confirm(FRAME, LANG.getString("drive.jop.wrongDrivePass"))) {
                String confirm = DIALOG.input(FRAME, LANG.getString("drive.jop.deleteAllDrivePasswords"));
                if (confirm.replace("'", "").equals(LANG.getString("drive.jop.deleteAllDrivePasswords").split("'")[1])) {
                    driveServiceHelper.deletePassFile();
                    synchronize();
                }
            }
        } else {
            // DRIVE_PASSWORD is set -> Reset it
            PassProperty.DRIVE_PASSWORD.setValue("", PassFrame.aes);
            synchronize();
        }
    }

    /**
     * Creates/Searches the drive password.
     * If pass file doesn't exist, a new password will be generated ({@link DriveLocalHelper#generateDrivePass(long)})
     * else the password is either stored inside {@link PassProperty#DRIVE_PASSWORD} or the user has to input it
     *
     * @return The found password
     */
    private AES getDriveAES() {
        AES drivePass;
        if (driveServiceHelper.passFileExists()) {
            // Pass file exists -> Either the password is stored inside PassProperty or the user has to input it
            String inputPass = PassProperty.DRIVE_PASSWORD.getValue().isEmpty() ? DIALOG.input(FRAME, LANG.getString("drive.jop.enterDrivePass"), true) : PassProperty.DRIVE_PASSWORD.getValue();
            if (inputPass == null) {
                return null;
            }
            drivePass = new AES(inputPass);
        } else {
            // Pass file doesn't exist -> pass will be generated
            drivePass = new AES(generateDrivePass(Boolean.parseBoolean(PassProperty.REAL_RANDOM.getValue()) ? RealRandom.seedWithUserInput(FRAME, LANG.getString("generate.jop.realRandom")) : -1));
            DIALOG.message(FRAME, String.format(LANG.getString("drive.jop.createdDrivePass"), drivePass.getPass()));
        }
        return drivePass;
    }

    /**
     * Starts the animation via a {@link ScheduledExecutorService} and
     * stops it when {@link DriveLocalHelper#stopAnimation} == {@code true} and the animation can be stopped
     *
     * @see Animation#isFinished()
     */
    private void startAnimation() {
        stopAnimation = false;
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Animation driveAnimation = ((PassFrame) FRAME).driveAnimation;
            driveAnimation.angle++;
            if (stopAnimation && driveAnimation.isFinished()) {
                scheduledExecutorService.shutdown();
                driveAnimation.stop();
            }
            COMPONENTS.get("passFrame.lbl.refresh").repaint();
        }, 0, 4, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the animation via {@link DriveLocalHelper#stopAnimation} = {@code true} and thus leads to animation to a stop
     */
    private void stopAnimation() {
        stopAnimation = true;
    }

    /**
     * Compares a given list of {@link Password} objects to the passwords stored on Google Drive,
     * then stores the compared passwords to Google Drive and returns this very list of compared {@link Password} objects
     *
     * @param local     The list of {@link Password} objects to be compared to the passwords stored on Google Drive
     * @param drivePass The {@link AES} object containing the drive password
     * @return The compared password list
     * @throws IOException                Error while reading/storing Google Drive
     * @throws AES.WrongPasswordException Wrong drive password
     */
    private List<Password> compareToDriveAndSync(List<Password> local, AES drivePass) throws IOException, AES.WrongPasswordException {
        List<Password> compared = driveServiceHelper.passFileExists() ? CompareVaults.compare(local, driveServiceHelper.readPasswords(drivePass)) : local;
        driveServiceHelper.savePasswords(compared, drivePass);
        String[] compareLog = CompareVaults.changeLog(local, compared);
        for (String s : compareLog) {
            LOG.info(s);
        }
        return compared;
    }
}
