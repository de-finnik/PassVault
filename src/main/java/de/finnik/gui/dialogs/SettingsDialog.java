package de.finnik.gui.dialogs;

import de.finnik.AES.AES;
import de.finnik.gui.PopUp;
import de.finnik.gui.Var;
import de.finnik.gui.mainFrame.PassFrame;
import de.finnik.passvault.InactivityListener;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.PassUtils;
import de.finnik.passvault.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.finnik.gui.Var.*;

/**
 * Change settings, get information and help about the application
 */
public class SettingsDialog extends JDialog {

    /**
     * A list of {@link javax.swing.JComponent}s which are generated in {@link SettingsDialog#components()}
     * to be added to the content pane in the right order {@link SettingsDialog#positionComponents(List)}
     */
    private final List<JComponent> components;

    /**
     * Creates the frame
     */
    public SettingsDialog(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createLineBorder(FOREGROUND));
        contentPane.setBackground(BACKGROUND);
        setContentPane(contentPane);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        components = new ArrayList<>();
        components();
        textComponents();

        positionComponents(components);

        // Sets color for labels and buttons
        PassUtils.GUIUtils.colorComponents(getMatchingComponents("settings.lbl", "settings.btn", "settings.check"), FOREGROUND, BACKGROUND);

        for (Component toolBar : getMatchingComponents("settings.toolBar")) {
            for (Component label : ((JPanel) toolBar).getComponents()) {
                label.setCursor(HAND_CURSOR);
            }
        }

        adjustSizeAndCenter();
    }

    /**
     * Lets the user change his main password by entering it two times
     */
    public static void changeMainPass(Window owner) {
        CreatePasswordDialog createPasswordDialog = new CreatePasswordDialog(owner, LANG.getString("jop.enterNewMainPass"), Arrays.stream(Var.class.getFields()).filter(name -> Arrays.asList("CLOSE", "HIDE", "SHOW").contains(name.getName())).collect(Collectors.toMap(Field::getName, f -> {
            try {
                return (BufferedImage) f.get(f);
            } catch (IllegalAccessException e) {
                return CLOSE;
            }
        })), d -> DIALOG.confirm(d, LANG.getString("jop.useWeakMainPass")));
        createPasswordDialog.font = raleway(15);
        char[] chars = createPasswordDialog.open();
        if (!FRAME.isVisible())
            return;
        String mainPass = new String(chars);
        if (!mainPass.isEmpty()) {
            String validation = DIALOG.input(owner, LANG.getString("jop.repeatEnteringNewMainPass"), true);
            if (mainPass.equals(validation)) {
                PassFrame.aes = new AES(mainPass);
                LOG.info("Changed main password!");
                PassFrame.savePasswords();
                PassProperty.store(PassFrame.aes);
                INACTIVITY_LISTENER = new InactivityListener(Integer.parseInt(PassProperty.INACTIVITY_TIME.getValue()), () -> ((PassFrame) FRAME).inactive());
                INACTIVITY_LISTENER.start();
            } else {
                DIALOG.message(owner, LANG.getString("jop.wrongPass"));
            }
        }
    }

    private void components() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolBar.setBackground(BACKGROUND);
        add(toolBar, "settings.toolBar");

        JLabel lblDrive = new JLabel();
        lblDrive.setIcon(new ImageIcon(DRIVE_ICON));
        lblDrive.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        lblDrive.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    try {
                        DRIVE.synchronize(() -> {
                            ((PassFrame) FRAME).passBankPanel.updateTableModel();
                            ((PassFrame) FRAME).refreshDriveVisibility();
                        });
                    } catch (Exception exception) {
                        LOG.error("Error while synchronizing with Drive", exception);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (!PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
                        new PopUp.PassPopUp(new PopUp.PopUpItem(LANG.getString("settings.pop.disableDrive"), action -> {
                            PassProperty.DRIVE_PASSWORD.setValue("", PassFrame.aes);
                            if (new File(APP_DIR, "StoredCredential").delete()) {
                                LOG.info("Deleted StoredCredential");
                            }
                            ((PassFrame) FRAME).refreshDriveVisibility();
                            textComponents();
                            refreshShowPassTextField();
                        })).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        if (PassFrame.aes.passIsSet())
            toolBar.add(lblDrive);

        JLabel lblExtract = new JLabel();
        lblExtract.setIcon(new ImageIcon(EXTRACT));
        lblExtract.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (PassFrame.passwordList.size() == 0)
                    return;
                try {
                    HINTS.triggerHint("hints.settings.backup", key -> DIALOG.message(SettingsDialog.this, LANG.getString(key)));
                } catch (IOException ioException) {
                    LOG.error("Error while loading hints", ioException);
                }
                // Choose the directory to store the update into
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = jfc.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION && PassFrame.aes.passIsSet() && PassFrame.passwordList.size() > 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
                    Date date = new Date();
                    File backup = new File(jfc.getSelectedFile(), "PassVault_" + formatter.format(date) + ".bin");

                    // Check whether target file exists already
                    if (!backup.exists()) {
                        Password.savePasswords(PassFrame.passwordList, backup, PassFrame.aes);
                        LOG.info("Exported password to {}!", jfc.getSelectedFile().getAbsolutePath());
                    } else {
                        DIALOG.message(SettingsDialog.this, String.format(LANG.getString("jop.fileExistsAlready"), backup.getAbsolutePath()));
                    }
                }
            }
        });
        toolBar.add(lblExtract);

        JLabel lblHelp = new JLabel();
        lblHelp.setIcon(new ImageIcon(HELP));
        lblHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Open help page
                String url = "https://finnik.de/passvault";
                try {
                    Utils.Browser.browse(url);
                } catch (Exception ex) {
                    LOG.error("Error while opening help page!", ex);
                }

            }
        });
        toolBar.add(lblHelp);

        JLabel lblClose = new JLabel();
        lblClose.setIcon(new ImageIcon(Utils.resizeImage(CLOSE, 20, 20)));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        toolBar.add(lblClose);

        JLabel lblIcon = new JLabel();
        lblIcon.setIcon(new ImageIcon(ICON));
        lblIcon.setSize(lblIcon.getPreferredSize());
        add(lblIcon, "settings.lbl.icon");

        JPanel panelVersion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelVersion.setBackground(BACKGROUND);

        JLabel lblVersion = new JLabel(APP_INFO.getProperty("app.name") + " " + APP_INFO.getProperty("app.version"));
        lblVersion.setFont(raleway(13));
        lblVersion.setSize(lblVersion.getPreferredSize());
        COMPONENTS.put("settings.lbl.version", lblVersion);
        panelVersion.add(lblVersion);

        JLabel lblFinnik = new JLabel();
        lblFinnik.setIcon(new ImageIcon(FINNIK));
        lblFinnik.setSize(lblFinnik.getPreferredSize());
        panelVersion.add(lblFinnik);

        panelVersion.setSize(new Dimension(lblVersion.getSize().width + lblFinnik.getSize().width, lblVersion.getSize().height + lblFinnik.getSize().height));
        components.add(panelVersion);


        JButton btnChangeMainPass = new JButton();
        btnChangeMainPass.setFont(raleway(13));
        btnChangeMainPass.addActionListener(action -> {
            if (PassFrame.aes.passIsSet()) {
                // Validates the user via inserting current main pass
                String mainPass = DIALOG.input(this, LANG.getString("check.lbl.pass"), true);
                if (mainPass == null) {
                    return;
                }
                if (mainPass.equals(PassFrame.aes.getPass())) {
                    changeMainPass(SettingsDialog.this);
                } else if (!mainPass.isEmpty()) {
                    DIALOG.message(this, LANG.getString("jop.wrongPass"));
                }
            } else {
                changeMainPass(SettingsDialog.this);
            }
        });
        add(btnChangeMainPass, "settings.btn.changeMainPass");

        DefaultComboBoxModel<String> comboBoxLanguageModel = new DefaultComboBoxModel<>();
        JComboBox<String> comboBoxLanguage = new JComboBox<>(comboBoxLanguageModel);
        comboBoxLanguage.setFont(raleway(15));
        // Find all available languages and add them to the combo box model
        List<Locale> locales = PassUtils.FileUtils.availableLanguages().stream().map(Locale::new).collect(Collectors.toList());
        locales.stream().map(Locale::getDisplayName).forEach(comboBoxLanguageModel::addElement);
        comboBoxLanguage.setSelectedItem(new Locale(PassProperty.LANG.getValue()).getDisplayLanguage());
        comboBoxLanguage.addActionListener(action -> {
            // Change language
            locales.stream()
                    .filter(locale -> locale.getDisplayLanguage().equals(comboBoxLanguage.getSelectedItem()))
                    .forEach(locale -> PassProperty.LANG.setValue(locale.getLanguage(), PassFrame.aes));
            LANG = loadLang();
            textComponents();
            adjustSizeAndCenter();
        });
        comboBoxLanguage.setBackground(FOREGROUND);
        comboBoxLanguage.setForeground(BACKGROUND);
        comboBoxLanguage.setMaximumSize(comboBoxLanguage.getPreferredSize());
        add(comboBoxLanguage, "settings.combo.language");

        JPanel panelInactivity = new JPanel(new FlowLayout());
        panelInactivity.setBackground(BACKGROUND);
        add(panelInactivity, "settings.panel.inactivity");

        JCheckBox checkBoxInactivityLock = new JCheckBox();
        checkBoxInactivityLock.setSelected(Boolean.parseBoolean(PassProperty.INACTIVITY_LOCK.getValue()));
        checkBoxInactivityLock.addActionListener(action -> {
            PassProperty.INACTIVITY_LOCK.setValue(checkBoxInactivityLock.isSelected(), PassFrame.aes);
            if (INACTIVITY_LISTENER != null)
                INACTIVITY_LISTENER.start();
        });
        checkBoxInactivityLock.setFont(raleway(13));
        COMPONENTS.put("settings.check.inactivityLock", checkBoxInactivityLock);
        panelInactivity.add(checkBoxInactivityLock);

        JSpinner spinnerInactivityTime = new JSpinner(new SpinnerNumberModel(Integer.parseInt(PassProperty.INACTIVITY_TIME.getValue()), 10, 3600, 1));
        spinnerInactivityTime.setPreferredSize(new Dimension(50, 20));
        spinnerInactivityTime.setFont(raleway(11));
        spinnerInactivityTime.setBorder(BorderFactory.createEmptyBorder());
        ((JSpinner.DefaultEditor) spinnerInactivityTime.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        spinnerInactivityTime.setValue(Integer.valueOf(((JFormattedTextField) e.getComponent()).getText()));
                } catch (NumberFormatException ignored) {

                }
            }
        });
        spinnerInactivityTime.addChangeListener(e -> {
            if (!PassProperty.INACTIVITY_TIME.setValue(spinnerInactivityTime.getValue(), PassFrame.aes)) {
                DIALOG.message(this, String.format(LANG.getString("settings.jop.noValidInactivityTime"), ((SpinnerNumberModel) spinnerInactivityTime.getModel()).getMinimum(), ((SpinnerNumberModel) spinnerInactivityTime.getModel()).getMaximum()));
            } else {
                INACTIVITY_LISTENER.setInactivity(Integer.parseInt(PassProperty.INACTIVITY_TIME.getValue()));
            }
        });
        COMPONENTS.put("settings.spinner.inactivityTime", spinnerInactivityTime);
        panelInactivity.add(spinnerInactivityTime);

        JLabel lblInactivityLock = new JLabel();
        lblInactivityLock.setFont(raleway(13));
        COMPONENTS.put("settings.lbl.inactivityLock", lblInactivityLock);
        panelInactivity.add(lblInactivityLock);

        JCheckBox checkBoxDottedPasswords = new JCheckBox();
        checkBoxDottedPasswords.setSelected(Boolean.parseBoolean(PassProperty.SHOW_PASSWORDS_DOTTED.getValue()));
        checkBoxDottedPasswords.addActionListener(action -> {
            PassProperty.SHOW_PASSWORDS_DOTTED.setValue(checkBoxDottedPasswords.isSelected(), PassFrame.aes);
            ((PassFrame) FRAME).passBankPanel.updateTableModel();
        });
        checkBoxDottedPasswords.setFont(raleway(13));
        add(checkBoxDottedPasswords, "settings.check.dottedPasswords");

        JCheckBox checkBoxShowMainPass = new JCheckBox();
        checkBoxShowMainPass.setSelected(Boolean.parseBoolean(PassProperty.SHOW_MAIN_PASSWORD.getValue()));
        checkBoxShowMainPass.addActionListener(action -> PassProperty.SHOW_MAIN_PASSWORD.setValue(checkBoxShowMainPass.isSelected(), PassFrame.aes));
        checkBoxShowMainPass.setFont(raleway(13));
        add(checkBoxShowMainPass, "settings.check.showMainPass");

        JCheckBox checkBoxRealRandom = new JCheckBox();
        checkBoxRealRandom.setSelected(Boolean.parseBoolean(PassProperty.REAL_RANDOM.getValue()));
        checkBoxRealRandom.addActionListener(action -> PassProperty.REAL_RANDOM.setValue(checkBoxRealRandom.isSelected(), PassFrame.aes));
        checkBoxRealRandom.setFont(raleway(13));
        add(checkBoxRealRandom, "settings.check.realRandom");

        JButton btnDrivePassword = new JButton();
        btnDrivePassword.addActionListener(action -> {
            // If password is already displayed, the font will be changed to ensure better readability
            if (!btnDrivePassword.getText().equals(LANG.getString("settings.btn.drivePassword"))) {
                String[] availableFonts = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()).map(Font::getName).toArray(String[]::new);
                btnDrivePassword.setFont(new Font(availableFonts[new Random().nextInt(availableFonts.length)], Font.PLAIN, 15));
                return;
            }
            if (PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
                return;
            }
            // Validates main password
            String mainPass = DIALOG.input(this, LANG.getString("check.lbl.pass"), true);
            if (mainPass.equals(PassFrame.aes.getPass())) {
                btnDrivePassword.setText(PassProperty.DRIVE_PASSWORD.getValue());
            } else if (!mainPass.isEmpty()) {
                DIALOG.message(this, LANG.getString("jop.wrongPass"));
            }
        });
        btnDrivePassword.setFont(raleway(15));
        add(btnDrivePassword, "settings.btn.drivePassword");
        refreshShowPassTextField();
    }

    /**
     * Adds a component with its name to the {@link Var#COMPONENTS} map and adds the component to the panel
     * The method kind of overwrites {@link java.awt.Container#add(Component)} method in order to handle the components later
     *
     * @param c   The component
     * @param key The component´s matching name
     */
    private void add(JComponent c, String key) {
        COMPONENTS.put(key, c);
        components.add(c);
    }

    /**
     * Adds a set of {@link javax.swing.JComponent}s to the pane and creates a gap between them
     *
     * @param components The components
     */
    private void positionComponents(List<JComponent> components) {
        components.forEach(component -> {
            component.setAlignmentX(Component.CENTER_ALIGNMENT);
            getContentPane().add(component);
            getContentPane().add(Box.createVerticalGlue());
        });
    }

    /**
     * Adjusts the dialog's size and centers it via {@link Window#setLocationRelativeTo(Component)}
     */
    private void adjustSizeAndCenter() {
        setSize(new Dimension((getContentPane().getLayout().preferredLayoutSize(getContentPane())).width + 50, (getContentPane().getLayout().preferredLayoutSize(getContentPane())).height + 60));
        setLocationRelativeTo(null);
    }

    /**
     * Checks whether {@link PassProperty#DRIVE_PASSWORD} is empty and toggles the visibility of the show drive password btn
     */
    private void refreshShowPassTextField() {
        COMPONENTS.get("settings.btn.drivePassword").setVisible(!PassProperty.DRIVE_PASSWORD.getValue().isEmpty());
    }
}
