package de.finnik.gui;

import de.finnik.AES.AES;
import de.finnik.drive.PassDrive;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.Password;
import de.finnik.passvault.Utils;

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
import java.util.stream.Collectors;

import static de.finnik.gui.Var.*;

/**
 * The heart of the PassVault application.
 * It's the main frame where you can generate new passwords ({@link GeneratePasswordPanel}) or search for the password that you need ({@link PassBankPanel}).
 */
public class PassFrame extends JFrame {

    /**
     * The main password
     */
    public static String password;
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
     * @param password     The main password which will be saved to {@link PassFrame#password}
     * @param passwordList The list of {@link Password}s which will be saved to {@link PassFrame#passwordList}
     */
    PassFrame(String password, List<Password> passwordList) {
        PassFrame.password = password;
        PassFrame.aes = new AES(password);
        PassFrame.passwordList = new ArrayList<>(passwordList);

        setContentPane(new JPanel());
        ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(FOREGROUND));

        getContentPane().setLayout(null);
        getContentPane().setBackground(BACKGROUND);
        setTitle("PassVault");
        setResizable(false);
        setUndecorated(true);

        setSize(710, 460);
        setLocationRelativeTo(null);

        MouseAdapter moveListener = new MouseAdapter() {
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
                    jfc.setFileFilter(new FileNameExtensionFilter(".bin", "bin"));
                    int result = jfc.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION && PassFrame.password.length() > 0) {
                        importBackup(jfc.getSelectedFile());
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

        addMouseListener(moveListener);
        addMouseMotionListener(moveListener);

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
                    importBackup(file);
                }
                return true;
            }
        });

        driveAnimation = new Animation(REFRESH);
        components();
        textComponents();

        Arrays.stream(getMatchingComponents("passFrame.lbl"))
                .forEach(c -> c.setCursor(Var.HAND_CURSOR));

        INACTIVITY_LISTENER.start();
    }

    /**
     * Saves the password stored in {@link PassFrame#passwordList} encrypted with the password {@link PassFrame#password}
     * to the passwords file {@link Var#PASSWORDS}
     */
    public static void savePasswords() {
        Password.savePasswords(passwordList, PASSWORDS, password);
        if (!PassProperty.DRIVE_PASSWORD.getValue().isEmpty())
            PassDrive.compare(((PassFrame) FRAME).passBankPanel::updateTableModel);
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
        refreshVisibility();

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
        if (!Boolean.parseBoolean(PassProperty.INACTIVITY_LOCK.getValue()) || password.length() == 0)
            return;

        Component[] toHide = {this, COMPONENTS.get("savePass"), COMPONENTS.get("settings")};
        for (Component component : toHide) {
            if (component != null)
                component.setVisible(false);
        }

        String pass = DIALOG.input(LANG.getString("check.lbl.pass"), true);
        if (!pass.equals(password)) {
            LOG.info("User tried to log in with wrong password!");
            System.exit(0);
        }
        INACTIVITY_LISTENER.start();
        for (Component component : toHide) {
            if (component != null)
                component.setVisible(true);
        }
    }

    public void refreshVisibility() {
        COMPONENTS.get("passFrame.lbl.refresh").setVisible(!PassProperty.DRIVE_PASSWORD.getValue().isEmpty());
    }

    /**
     * Import a backup from a file
     *
     * @param file The location of the backup
     */
    private void importBackup(File file) {
        if (file.getName().endsWith(".bin")) {
            String pass = DIALOG.input(LANG.getString("passFrame.jop.enterPass"), true);
            try {
                PassFrame.passwordList.addAll(Password.readPasswords(file, pass).stream().filter(p -> PassFrame.passwordList.stream().noneMatch(p1 -> p1.id().equals(p.id()))).collect(Collectors.toList()));
                LOG.info("Imported passwords from {}!", file.getAbsolutePath());
                savePasswords();
            } catch (AES.WrongPasswordException e) {
                if (pass.length() > 0)
                    DIALOG.message(LANG.getString("jop.wrongPass"));
            }
        } else {
            DIALOG.message(LANG.getString("passFrame.jop.noSupportedFile"));
        }
    }
}
