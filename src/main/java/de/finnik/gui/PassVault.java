package de.finnik.gui;

import de.finnik.passvault.*;
import org.slf4j.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.List;
import java.util.*;

import static de.finnik.gui.Var.*;
import static de.finnik.passvault.Utils.*;

/**
 * The PassVault program implements an application that saves passwords encrypted
 * with a main password. It also generates new passwords with a given length and given parameters.
 *
 * @author finnik
 * @version 1.0
 * @since 21.03.2020
 */

public class PassVault {

    public static void main(String[] args) {

        LOG = LoggerFactory.getLogger(PassVault.class);

        System.setErr(new PrintStream(new LogErrorStream(LOG)));

        PassVault main = new PassVault();
        LOG.info("Welcome to PassVault, we´re happy to see you!");
        main.run();
    }

    /**
     * Creates necessary files (if they don´t already exist), loads fonts, images and properties,
     * creates necessary instances for variables in {@link de.finnik.gui.Var} and starts the application.
     */
    private void run() {
        final File dir = new File("bin");

        PASSWORDS = new File(dir, "pass");
        try {
            if (PASSWORDS.createNewFile()) {
                LOG.info("Created pass file in main directory! ({})!", PASSWORDS.getAbsolutePath());
            }
        } catch (Exception e) {
            LOG.error("Error while creating pass file in APPDATA!", e);
        }

        APP_INFO = new Properties();
        try {
            APP_INFO.load(new InputStreamReader(PassVault.class.getResourceAsStream("/application.properties"), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("Error while reading application.properties!", e);
        }


        PROPERTIES = new File(dir, "config.properties");
        try {
            if (PROPERTIES.createNewFile()) {
                LOG.info("Created config.properties in main directory ({})!", PROPERTIES.getAbsolutePath());
            }
        } catch (Exception e) {
            LOG.error("Error while reading config.properties");
        }

        PROPS = PassUtils.FileUtils.getDefaultProperties();
        try {
            PROPS.load(new FileReader(PROPERTIES));
            LOG.info("Loaded properties!");
            PassUtils.FileUtils.validateProperties(PROPS);
        } catch (Exception e) {
            LOG.error("Error while loading properties from config.properties file!", e);
        }

        LANG = loadLang();

        try (InputStream is = getClass().getResourceAsStream("/fonts/Raleway-Regular.ttf")) {
            RALEWAY = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            LOG.error("Error while loading Raleway font!", e);
        }

        loadImages(Arrays.stream(Var.class.getFields()).filter(field -> field.getType() == BufferedImage.class).toArray(Field[]::new));

        DIALOG = new PassDialog(FOREGROUND, BACKGROUND, RALEWAY, CLOSE, WARNING, ICON_SMALL, QUESTION_MARK, CHECK_MARK);

        COMPONENTS = new HashMap<>();

        EventQueue.invokeLater(() -> {

            UIManager.put("ToolTip.background", FOREGROUND);
            UIManager.put("ToolTip.foreground", BACKGROUND);
            UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(2, 2, 2, 2));

            /*
            Reads the {@link de.finnik.gui.Var#PASSWORDS} file, checks whether passwords are already saved and creates either the login dialog
            or the main frame with an empty string as the main password
             */
            try (BufferedReader br = new BufferedReader(new FileReader(PASSWORDS))) {
                String file = br.readLine();
                if (file != null) {
                    FRAME = new CheckFrame();
                } else {
                    FRAME = new PassFrame("", new ArrayList<>());
                }
                FRAME.setVisible(true);
            } catch (Exception e) {
                LOG.error("Error while reading password file!", e);
            }
        });
    }

    /**
     * Loads all images from its fields (The matching file has the same name as the field)
     *
     * @param images The image fields that should be loaded
     */
    private void loadImages(Field[] images) {
        for (Field img : images) {
            String res = String.format("/images/%s.png", img.getName().toLowerCase());
            try (InputStream is = PassVault.class.getResourceAsStream(res)) {
                img.set(null, ImageIO.read(is));
            } catch (Exception e) {
                LOG.error("Error while reading {}!", res, e);
            }
        }
    }

    /**
     * Login frame that wants you to enter your main password that is used to encrypt all passwords.
     * It´s created on beginning and you won´t enter {@link de.finnik.gui.PassFrame} unless you haven´t typed in the correct password.
     */
    public static class CheckFrame extends JDialog {
        /**
         * Creates the frame
         */
        CheckFrame() {
            setSize(380, 200);
            setTitle("Login");
            setLocationRelativeTo(null);

            setContentPane(new JPanel());
            getContentPane().setLayout(null);
            ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(FOREGROUND));

            setResizable(false);
            getContentPane().setBackground(BACKGROUND);
            setUndecorated(true);

            components();
            textComponents();
        }

        /**
         * Generates the components
         */
        private void components() {
            JLabel lblIcon = new JLabel();
            lblIcon.setIcon(new ImageIcon(ICON));
            lblIcon.setSize(lblIcon.getPreferredSize());
            lblIcon.setBounds(Utils.getCentralPosition(getWidth(), lblIcon.getWidth()), 10, lblIcon.getWidth(), lblIcon.getHeight());
            add(lblIcon, "check.lbl.icon");

            JLabel lblClose = new JLabel();
            lblClose.setIcon(new ImageIcon(CLOSE));
            lblClose.setSize(lblClose.getPreferredSize());
            lblClose.setBounds(getWidth() - lblClose.getWidth() - 10, 10, lblClose.getWidth(), lblClose.getHeight());
            lblClose.setCursor(HAND_CURSOR);
            lblClose.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    System.exit(0);
                }
            });
            add(lblClose, "check.lbl.close");

            JLabel lblPass = new JLabel();
            add(lblPass, "check.lbl.pass");
            textComponents();
            lblPass.setFont(raleway(15));
            lblPass.setSize(lblPass.getFontMetrics(lblPass.getFont()).stringWidth(lblPass.getText()), 50);
            lblPass.setBounds(Utils.getCentralPosition(getWidth(), lblPass.getWidth()), 70, lblPass.getWidth(), lblPass.getHeight());
            lblPass.setForeground(FOREGROUND);


            JButton btnLogin = new JButton();

            JTextField passwordField = new JPasswordField();
            passwordField.setBounds(10, 120, 360, 30);
            passwordField.setFont(raleway(20));
            passwordField.setBorder(BorderFactory.createLineBorder(Color.black));
            passwordField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        btnLogin.doClick();
                    }
                }
            });
            add(passwordField, "check.pf.password");

            btnLogin.setFont(sizeFont(RALEWAY, 15));
            btnLogin.setSize(200, 30);
            btnLogin.setForeground(BACKGROUND);
            btnLogin.setBackground(FOREGROUND);
            btnLogin.setBounds(Utils.getCentralPosition(getWidth(), btnLogin.getWidth()), 160, btnLogin.getWidth(), btnLogin.getHeight());
            btnLogin.addActionListener(action -> {
                // The login
                List<Password> passwordList;
                try {
                    passwordList = Password.readPasswords(PASSWORDS, passwordField.getText());
                } catch (IllegalArgumentException e) {
                    // Exception -> Wrong password
                    LOG.info("User tried to log in with wrong password!");
                    passwordField.setText("");
                    DIALOG.message(this, LANG.getProperty("jop.wrongPass"));
                    return;
                }
                LOG.info("User logged in!");
                dispose();

                FRAME = new PassFrame(passwordField.getText(), passwordList);
                FRAME.setVisible(true);
            });
            add(btnLogin, "check.btn.login");
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
    }
}
