package de.finnik.gui;

import com.sun.java.swing.plaf.windows.*;
import de.finnik.passvault.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;

import static de.finnik.gui.Var.*;

/**
 * The heart of the PassVault application.
 * It's the main frame where you can generate new passwords ({@link de.finnik.gui.GeneratePasswordPanel}) or search for the password that you need ({@link de.finnik.gui.PassBankPanel}).
 */
public class PassFrame extends JFrame {

    /**
     * The main password
     */
    public static String password;
    /**
     * The list of saved passwords
     */
    public static List<Password> passwordList;

    /**
     * Creates the frame
     *
     * @param password     The main password which will be saved to {@link de.finnik.gui.PassFrame#password}
     * @param passwordList The list of {@link de.finnik.passvault.Password}s which will be saved to {@link de.finnik.gui.PassFrame#passwordList}
     */
    PassFrame(String password, List<Password> passwordList) {
        PassFrame.password = password;
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
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ex) {
                        LOG.error("Error while setting look and feel!", ex);
                    }
                    JFileChooser jfc = new JFileChooser() {
                        @Override
                        public int showOpenDialog(Component parent) throws HeadlessException {
                            return super.showOpenDialog(parent);
                        }
                    };
                    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jfc.setFileFilter(new FileNameExtensionFilter(".bin", "bin"));
                    int result = jfc.showOpenDialog(null);
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception ex) {
                        LOG.error("Error while setting look and feel!", ex);
                    }
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

        components();
        textComponents();

        Arrays.stream(getMatchingComponents("passFrame.lbl"))
                .forEach(c -> c.setCursor(Var.HAND_CURSOR));
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

        JLabel lblInfo = new JLabel();
        lblInfo.setIcon(new ImageIcon(INFO));
        lblInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JDialog settingsDialog = new SettingsDialog(FRAME);
                COMPONENTS.put("settings", settingsDialog);
                settingsDialog.setVisible(true);
            }
        });
        lblInfo.setBounds(10, 10, 30, 30);
        add(lblInfo, "passFrame.lbl.info");

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

        JPanel passBankPanel = new PassBankPanel();
        passBankPanel.setBounds(350, 100, 350, 350);
        getContentPane().add(passBankPanel);
    }

    /**
     * Adds a component with its name to the {@link de.finnik.gui.Var#COMPONENTS} map and adds the component to the panel
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
     * Saves the password stored in {@link de.finnik.gui.PassFrame#passwordList} encrypted with the password {@link de.finnik.gui.PassFrame#password}
     * to the passwords file {@link de.finnik.gui.Var#PASSWORDS}
     */
    static void savePasswords() {
        Password.savePasswords(passwordList, PASSWORDS, password);
    }

    /**
     * Import a backup from a file
     *
     * @param file The location of the backup
     */
    private void importBackup(File file) {
        if (file.getName().endsWith(".bin")) {
            DIALOG.input(FRAME, LANG.getProperty("passFrame.jop.enterPass"), pass -> {
                try {
                    PassFrame.passwordList.addAll(Password.readPasswords(file, pass).stream().filter(p -> !PassFrame.passwordList.contains(p)).collect(Collectors.toList()));
                    LOG.info("Imported passwords from {}!", file.getAbsolutePath());
                    Password.savePasswords(PassFrame.passwordList, PASSWORDS, password);
                    PassBankPanel.updateTableModel();
                } catch (Exception e) {
                    if (pass.length() > 0)
                        DIALOG.message(FRAME, LANG.getProperty("jop.wrongPass"));
                }
            }, true);
        } else {
            DIALOG.message(FRAME, LANG.getProperty("passFrame.jop.noSupportedFile"));
        }
    }
}
