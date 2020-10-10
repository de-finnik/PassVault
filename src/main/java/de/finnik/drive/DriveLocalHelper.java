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

public class DriveLocalHelper {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    boolean stopAnimation = false;
    private DriveServiceHelper driveServiceHelper;

    private static String generateDrivePass(long seed) {
        return (seed >= 0 ? new PasswordGenerator(seed) : new PasswordGenerator()).generatePassword(12, PasswordGenerator.PassChars.BIG_LETTERS, PasswordGenerator.PassChars.SMALL_LETTERS, PasswordGenerator.PassChars.NUMBERS, PasswordGenerator.PassChars.SPECIAL_CHARACTERS);
    }

    public void synchronize(Runnable andThen) {
        synchronize();
        executorService.execute(andThen);
    }

    public void synchronize() {
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
                if (driveServiceHelper == null) {
                    INACTIVITY_LISTENER.stop();
                    driveServiceHelper = new DriveServiceHelper(DriveServiceHelper.Builder.buildDrive());
                    INACTIVITY_LISTENER.start();
                }
                startAnimation();
                AES drivePass;
                if (driveServiceHelper.passFileExists()) {
                    String inputPass = PassProperty.DRIVE_PASSWORD.getValue().isEmpty() ? DIALOG.input(FRAME, LANG.getString("drive.jop.enterDrivePass"), true) : PassProperty.DRIVE_PASSWORD.getValue();
                    if (inputPass == null || inputPass.isEmpty()) {
                        return;
                    }
                    drivePass = new AES(inputPass);
                } else {
                    drivePass = new AES(generateDrivePass(Boolean.parseBoolean(PassProperty.REAL_RANDOM.getValue()) ? new RealRandom().seedWithUserInput(FRAME, LANG.getString("generate.jop.realRandom")) : -1));
                    DIALOG.message(FRAME, String.format(LANG.getString("drive.jop.createdDrivePass"), drivePass.getPass()));
                }
                try {
                    PassFrame.passwordList = compare(PassFrame.passwordList, drivePass);
                    if (!PassProperty.DRIVE_PASSWORD.getValue().equals(drivePass.getPass())) {
                        PassProperty.DRIVE_PASSWORD.setValueAndStore(drivePass.getPass(), PassFrame.aes);
                    }
                    LOG.info("Synchronized with Drive");
                } catch (AES.WrongPasswordException e) {
                    if (PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
                        if (DIALOG.confirm(FRAME, LANG.getString("drive.jop.wrongDrivePass"))) {
                            String confirm = DIALOG.input(FRAME, LANG.getString("drive.jop.deleteAllDrivePasswords"));
                            if (confirm.replace("'", "").equals(LANG.getString("drive.jop.deleteAllDrivePasswords").split("'")[1])) {
                                driveServiceHelper.deletePassFile();
                                synchronize();
                            }
                        }
                    } else {
                        PassProperty.DRIVE_PASSWORD.setValueAndStore("", PassFrame.aes);
                        synchronize();
                    }
                }
                stopAnimation();
                ((PassFrame) FRAME).refreshDriveVisibility();
            } catch (Exception e) {
                LOG.error("Error while synchronizing with Drive", e);
            }
        };
        executorService.execute(run);
    }

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
        }, 0, 3, TimeUnit.MILLISECONDS);
    }

    private void stopAnimation() {
        stopAnimation = true;
    }

    private List<Password> compare(List<Password> local, AES drivePass) throws IOException {
        List<Password> compared = driveServiceHelper.passFileExists() ? CompareVaults.compare(local, driveServiceHelper.readPasswords(drivePass)) : local;
        driveServiceHelper.savePasswords(compared, drivePass);
        return compared;
    }
}
