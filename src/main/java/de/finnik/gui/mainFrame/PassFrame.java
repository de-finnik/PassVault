package de.finnik.gui.mainFrame;

import de.finnik.AES.AES;
import de.finnik.gui.Var;
import de.finnik.gui.customComponents.Animation;
import de.finnik.gui.dialogs.CompareDialog;
import de.finnik.gui.dialogs.SettingsDialog;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.finnik.gui.Var.*;

/**
 * The heart of the PassVault application.
 * It's the main frame where you can generate new passwords ({@link GeneratePasswordPanel}) or manage stored passwords ({@link PassBankPanel}).
 */
public class PassFrame extends JFrame {

    /**
     * The main password
     */
    public static AES aes;
    /**
     * The list of saved passwords
     */
    public static List<Password> passwordList;
    public PassBankPanel passBankPanel;

    public Animation driveAnimation;

    /**
     * Creates the frame
     *
     * @param aes          The {@link AES} object which will be saved to {@link PassFrame#aes}
     * @param passwordList The list of {@link Password}s which will be saved to {@link PassFrame#passwordList}
     */
    public PassFrame(AES aes, List<Password> passwordList) {
        PassFrame.aes = aes;
        PassFrame.passwordList = new ArrayList<>(passwordList);

        JPanel contentPane = new JPanel(null);
        contentPane.setBorder(BorderFactory.createLineBorder(FOREGROUND));
        contentPane.setBackground(BACKGROUND);
        setContentPane(contentPane);

        setTitle("PassVault");
        setResizable(false);
        setUndecorated(true);

        setSize(710, 460);
        setLocationRelativeTo(null);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point mouseDown = null;

            // Shift and right click -> import backup
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.isShiftDown() && e.getButton() == MouseEvent.BUTTON3) {
                    JFileChooser jfc = new JFileChooser() {
                        @Override
                        public int showOpenDialog(Component parent) throws HeadlessException {
                            return super.showOpenDialog(parent);
                        }
                    };
                    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jfc.setFileFilter(new FileNameExtensionFilter("PassVault backup", "bin"));
                    int result = jfc.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION && PassFrame.aes.passIsSet()) {
                        startImport(jfc.getSelectedFile());
                    }
                }
            }

            // Move the frame

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.getY() <= 25)
                    mouseDown = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                mouseDown = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (mouseDown != null) {
                    Point currentPos = e.getLocationOnScreen();
                    setLocation(currentPos.x - mouseDown.x, currentPos.y - mouseDown.y);
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        setIconImage(FRAME_ICON);
        setTransferHandler(new TransferHandler() {

            // Drag a file into the frame -> import backup

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                for (DataFlavor flavor : support.getDataFlavors()) {
                    if (flavor.isFlavorJavaFileListType()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!this.canImport(support))
                    return false;
                List<File> files;
                try {
                    files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                } catch (Exception ex) {
                    return false;
                }
                for (File file : files) {
                    startImport(file);
                }
                return true;
            }
        });

        driveAnimation = new Animation(REFRESH);
        components();
        textComponents();

        Arrays.stream(getMatchingComponents("passFrame.lbl"))
                .forEach(c -> c.setCursor(Var.HAND_CURSOR));

        if (INACTIVITY_LISTENER != null)
            INACTIVITY_LISTENER.start();
    }

    /**
     * Saves the password stored in {@link PassFrame#passwordList} encrypted with {@link PassFrame#aes}
     * to the passwords file {@link Var#PASSWORDS}
     */
    public static void savePasswords() {
        if (aes.passIsSet())
            Password.savePasswords(passwordList, PASSWORDS, aes);
        if (!PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
            try {
                DRIVE.synchronize(((PassFrame) FRAME).passBankPanel::updateTableModel);
            } catch (Exception e) {
                LOG.error("Error while synchronizing with Google Drive", e);
            }
        }
        ((PassFrame) FRAME).passBankPanel.updateTableModel();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        savePasswords();
    }

    /**
     * Generate the components
     */
    public void components() {
        JLabel lblLogo = new JLabel();
        lblLogo.setSize(355, 59);
        lblLogo.setBounds(Utils.getCentralPosition(getWidth(), lblLogo.getWidth()), 10, lblLogo.getWidth(), lblLogo.getHeight());
        lblLogo.setIcon(new ImageIcon(LOGO));
        getContentPane().add(lblLogo);

        JLabel lblSettings = new JLabel();
        lblSettings.setIcon(new ImageIcon(SETTINGS));
        lblSettings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JDialog settingsDialog = new SettingsDialog(FRAME);
                COMPONENTS.put("settings", settingsDialog);
                settingsDialog.setVisible(true);
            }
        });
        lblSettings.setBounds(10, 10, 30, 30);
        add(lblSettings, "passFrame.lbl.settings");

        JLabel lblRefresh = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(REFRESH_DRIVE, 0, 0, null);
                g.drawImage(driveAnimation.get(), 0, 0, null);
            }
        };
        lblRefresh.setIcon(new ImageIcon(REFRESH));
        lblRefresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                savePasswords();
            }
        });
        lblRefresh.setBounds(50, 10, 31, 30);
        add(lblRefresh, "passFrame.lbl.refresh");
        refreshDriveVisibility();

        JLabel lblClose = new JLabel();
        lblClose.setIcon(new ImageIcon(CLOSE));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                System.exit(0);
            }
        });
        lblClose.setSize(lblClose.getPreferredSize());
        lblClose.setBounds(getWidth() - lblClose.getWidth() - 10, 10, lblClose.getWidth(), lblClose.getHeight());
        add(lblClose, "passFrame.lbl.close");

        JLabel lblMinimize = new JLabel();
        BufferedImage minimize = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = minimize.createGraphics();
        g.setColor(FOREGROUND);
        g.fillRect(0, 15, minimize.getWidth(), 3);
        g.dispose();
        lblMinimize.setIcon(new ImageIcon(minimize));
        lblMinimize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                setState(ICONIFIED);
            }
        });
        lblMinimize.setSize(lblMinimize.getPreferredSize());
        lblMinimize.setBounds(lblClose.getX() - lblMinimize.getWidth() - 10, 10, lblMinimize.getWidth(), lblMinimize.getHeight());
        add(lblMinimize, "passFrame.lbl.minimize");

        JPanel generatePasswordPanel = new GeneratePasswordPanel();
        generatePasswordPanel.setBounds(10, 100, 300, 350);
        getContentPane().add(generatePasswordPanel);

        passBankPanel = new PassBankPanel();
        passBankPanel.setBounds(350, 100, 350, 350);
        getContentPane().add(passBankPanel);
    }

    /**
     * Adds a component with its name to the {@link Var#COMPONENTS} map and adds the component to the panel
     * The method kind of overwrites {@link java.awt.Container#add(Component)} method in order to handle the components later
     *
     * @param c   The component
     * @param key The component´s matching name
     */
    private void add(Component c, String key) {
        COMPONENTS.put(key, c);
        getContentPane().add(c);
    }

    /**
     * Is called when user is inactive. If property {@link PassProperty#INACTIVITY_LOCK} is true, PassVault will be locked
     * and can just be reentered by inputting the main password
     */
    public void inactive() {
        if (!Boolean.parseBoolean(PassProperty.INACTIVITY_LOCK.getValue()) || aes.getPass().length() == 0)
            return;

        DIALOG.disposeDialogs();
        setVisible(false);

        String pass = DIALOG.input(FRAME, LANG.getString("check.lbl.pass"), true);
        if (!pass.equals(aes.getPass())) {
            LOG.info("User tried to log in with wrong password!");
            System.exit(0);
        }
        INACTIVITY_LISTENER.start();
        setVisible(true);
    }

    /**
     * Refreshes the visibility of the drive label with the help of {@link PassProperty#DRIVE_PASSWORD}
     */
    public void refreshDriveVisibility() {
        COMPONENTS.get("passFrame.lbl.refresh").setVisible(!PassProperty.DRIVE_PASSWORD.getValue().isEmpty());
    }

    /**
     * Starts the import for a user selected file. Checks whether backup has a file extension of .bin,
     * whether the backup was encrypted with the same password as the users current master password
     * or else asks the user for the correct password.
     * <p>
     * When the correct password is found, {@link PassFrame#importBackup(File, AES)} is called
     *
     * @param file The location of the backup
     */
    private void startImport(File file) {
        if (file.getName().endsWith(".bin")) {
            try {
                importBackup(file, PassFrame.aes);
            } catch (AES.WrongPasswordException e) {
                AES aes = new AES(DIALOG.input(FRAME, LANG.getString("passFrame.jop.enterPass"), true));
                try {
                    importBackup(file, aes);
                } catch (AES.WrongPasswordException e1) {
                    DIALOG.message(FRAME, LANG.getString("jop.wrongPass"));
                }
            }
        } else {
            DIALOG.message(FRAME, LANG.getString("passFrame.jop.noSupportedFile"));
        }
    }

    /**
     * Imports a backup from a file
     *
     * @param file The location of the backup
     * @param aes  The aes object with that the backup is encrypted
     */
    private void importBackup(File file, AES aes) throws AES.WrongPasswordException {
        List<Password> backup = Password.readPasswords(file, aes);

        CompareDialog compareDialog = new CompareDialog(FRAME, PassFrame.passwordList, backup);
        COMPONENTS.put("compare", compareDialog);
        List<Password> compared = compareDialog.open();
        if (compared == null || !isVisible())
            return;
        PassFrame.passwordList = compared;
        LOG.info("Imported passwords from {}!", file.getAbsolutePath());
        savePasswords();
    }
}
