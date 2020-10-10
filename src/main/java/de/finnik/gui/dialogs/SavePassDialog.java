package de.finnik.gui.dialogs;

import de.finnik.gui.Var;
import de.finnik.gui.mainFrame.PassFrame;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.PassUtils;
import de.finnik.passvault.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.finnik.gui.Var.*;


/**
 * Save a new password with its parameters to your passwords.
 */
public class SavePassDialog extends JDialog {

    /**
     * A list of {@link javax.swing.JComponent}s which are generated in {@link SavePassDialog#components(String)}
     * to be added to the content pane in the right order {@link SavePassDialog#positionComponents(List)}
     */
    private final List<JComponent> components;

    /**
     * Creates the frame
     *
     * @param owner The window that owns the dialog
     * @param pass  A generated password that will be inserted to the Password {@link JTextField}
     */
    public SavePassDialog(Window owner, String pass) {
        super(owner, ModalityType.APPLICATION_MODAL);

        setContentPane(new JPanel());
        ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(FOREGROUND));
        getContentPane().setBackground(BACKGROUND);

        BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        getContentPane().setLayout(boxLayout);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        components = new ArrayList<>();
        components(pass);
        textComponents();

        positionComponents(components);

        // Sets border for textfields
        Arrays.stream(getMatchingComponents("savePass.tf"))
                .map(c -> ((JTextField) c))
                .forEach(tf -> tf.setBorder(BorderFactory.createLineBorder(Color.lightGray)));

        // Sets color for labels and buttons
        PassUtils.GUIUtils.colorComponents(getMatchingComponents("savePass.btn", "savePass.lbl"), FOREGROUND, BACKGROUND);

        // Sets font for labels and textfields
        for (Component component : getMatchingComponents("savePass.lbl", "savePass.tf")) {
            if (component.getClass() != JLabel.class) {
                // Redirects a 'enter' stroke to a button click
                component.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        super.keyPressed(e);
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            ((JButton) COMPONENTS.get("savePass.btn.save")).doClick();
                        }
                    }
                });
            }
            component.setFont(raleway(13));
        }

        setSize(new Dimension(boxLayout.preferredLayoutSize(getContentPane()).width + 100, boxLayout.preferredLayoutSize(getContentPane()).height + 60));

        ((JPanel) COMPONENTS.get("savePass.toolbar")).add(Box.createRigidArea(new Dimension((getWidth() - COMPONENTS.get("savePass.lbl.pass").getPreferredSize().width) / 2, 10)), BorderLayout.WEST);

        setLocationRelativeTo(null);
    }

    private void components(final String pass) {
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setBackground(BACKGROUND);
        add(toolBar, "savePass.toolbar");

        JLabel lblClose = new JLabel();
        lblClose.setIcon(new ImageIcon(Utils.resizeImage(CLOSE, 20, 20)));
        lblClose.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        lblClose.setCursor(HAND_CURSOR);
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        toolBar.add(lblClose, BorderLayout.EAST);

        JLabel lblPass = new JLabel();
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        COMPONENTS.put("savePass.lbl.pass", lblPass);
        toolBar.add(lblPass, BorderLayout.CENTER);

        JTextField tfPass = new JTextField(pass);
        add(tfPass, "savePass.tf.pass");

        JLabel lblSite = new JLabel();
        add(lblSite, "savePass.lbl.site");

        JTextField tfSite = new JTextField() {
            @Override
            public void addNotify() {
                super.addNotify();
                // Gets focus if password is given
                if (!pass.equals(""))
                    requestFocus();
            }
        };
        add(tfSite, "savePass.tf.site");

        JLabel lblUser = new JLabel();
        add(lblUser, "savePass.lbl.user");

        JTextField tfUser = new JTextField();
        add(tfUser, "savePass.tf.user");

        JLabel lblOther = new JLabel();
        add(lblOther, "savePass.lbl.other");

        JTextField tfOther = new JTextField();
        add(tfOther, "savePass.tf.other");

        JButton btnSave = new JButton();
        btnSave.setFont(raleway(15));
        btnSave.addActionListener(action -> {

            Password newPass = new Password(tfPass.getText(), tfSite.getText(), tfUser.getText(), tfOther.getText());

            if (!newPass.isEmpty()) {
                // Checks whether the user has set a main password
                if (!PassFrame.aes.passIsSet()) {
                    SettingsDialog.changeMainPass(SavePassDialog.this);
                }

                if (!PassFrame.aes.passIsSet()) {
                    return;
                }

                PassFrame.passwordList.add(newPass);
                LOG.info(Password.log(newPass, "Created password"));
            }

            PassFrame.savePasswords();
            try {
                PassUtils.copyToClipboard(this, newPass.getPass());
            } catch (IOException ioException) {
                LOG.error("Error while checking for hints!");
            }

            dispose();
        });
        add(btnSave, "savePass.btn.save");
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
            getContentPane().add(Box.createRigidArea(new Dimension(0, 5)));
        });
    }
}
