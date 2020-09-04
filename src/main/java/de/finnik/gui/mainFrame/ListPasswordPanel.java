package de.finnik.gui.mainFrame;

import de.finnik.gui.customComponents.BottomBorder;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.PassUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.finnik.gui.Var.*;

/**
 * This panel displays a list of given passwords
 */
public class ListPasswordPanel extends JPanel {
    JPanel passwords;

    /**
     * Initializes the panel
     */
    public ListPasswordPanel() {
        setLayout(null);
        setBackground(BACKGROUND);
    }

    /**
     * Displays the given list of passwords in the panel
     *
     * @param passwordList The passwords to display
     */
    public void display(List<Password> passwordList) {
        removeAll();

        passwords = new JPanel();
        JScrollPane scrollPane = new JScrollPane(passwords, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        BoxLayout boxLayout = new BoxLayout(passwords, BoxLayout.Y_AXIS);
        passwords.setLayout(boxLayout);
        passwords.setBackground(BACKGROUND);
        scrollPane.setBounds(0, 0, getWidth(), getHeight());
        add(scrollPane);

        for (int i = 0; i < passwordList.size(); i++) {
            passwords.add(convert(passwordList.get(i)));
            if (i + 1 < passwordList.size()) {
                passwords.add(Box.createVerticalStrut(10));
            }
        }

        validate();
        repaint();
    }

    /**
     * Returns the password that the user has selected.
     *
     * @return The selected password or {@code null}
     */
    public Password getSelectedPassword() {
        return Arrays.stream(passwords.getComponents())
                .filter(c -> c.getClass() == PasswordPanel.class)
                .filter(itemPanel -> ((PasswordPanel) itemPanel).isSelected())
                .map(itemPanel -> ((PasswordPanel) itemPanel).getPassword())
                .findAny().orElse(null);
    }

    /**
     * De-highlights all displaying panels and then highlights the given panel
     *
     * @param panel The panel to be highlighted after the process
     */
    private void highlight(PasswordPanel panel) {
        Arrays.stream(passwords.getComponents())
                .filter(c -> c.getClass() == PasswordPanel.class)
                .forEach(c -> ((PasswordPanel) c).setSelected(false));
        panel.setSelected(true);
    }

    /**
     * Converts a {@link Password} object into a {@link PasswordPanel} object
     *
     * @param password The {@link Password} object to convert
     * @return The converted {@link PasswordPanel} object
     */
    private PasswordPanel convert(Password password) {
        PasswordPanel outerPanel = new PasswordPanel(new BorderLayout(), password);
        outerPanel.setBorder(BorderFactory.createLineBorder(FOREGROUND));

        GridLayout gridLayout = new GridLayout(2, 2);
        gridLayout.setHgap(10);
        JPanel innerPanel = new JPanel(gridLayout);
        outerPanel.add(innerPanel, BorderLayout.CENTER);

        MouseAdapter highlight = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                highlight(outerPanel);
            }
        };

        innerPanel.addMouseListener(highlight);
        innerPanel.setBackground(BACKGROUND);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Stream.of(parameterToTextField(true, password.getPass(), password::setPass),
                parameterToTextField(false, password.getSite(), password::setSite),
                parameterToTextField(false, password.getUser(), password::setUser),
                parameterToTextField(false, password.getOther(), password::setOther))
                .forEach(tf -> {
                    tf.addMouseListener(highlight);
                    innerPanel.add(tf);
                });

        outerPanel.setSize(getWidth(), (getHeight() - 20) / 3);
        outerPanel.setMaximumSize(new Dimension(outerPanel.getMaximumSize().width, outerPanel.getHeight()));
        outerPanel.setPreferredSize(outerPanel.getSize());

        return outerPanel;
    }

    /**
     * Creates a {@link JTextField} to be added to a new {@link PasswordPanel} by taking a {@code String} to be displayed inside the TextField
     *
     * @param pass        {@link JTextField} -> {@code false}; {@link JPasswordField} -> {@code true}
     * @param parameter   The string to be displayed inside the TextField
     * @param keyListener A {@link Consumer} that takes a {@link String} and accepts the user input after he made changes to the TextField
     * @return The created {@link JTextField}
     */
    private JTextField parameterToTextField(boolean pass, String parameter, Consumer<String> keyListener) {
        JTextField textField;
        if (!pass) {
            textField = new JTextField(parameter);
        } else {
            textField = Boolean.parseBoolean(PassProperty.SHOW_PASSWORDS_DOTTED.getValue()) ? new JPasswordField(parameter) : new JTextField(parameter);
        }
        AtomicBoolean focus = new AtomicBoolean(false);
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    keyListener.accept(textField.getText());
                    PassFrame.savePasswords();
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE && focus.get()) {
                    deleteSelectedPassword();
                }
            }
        });
        textField.setEditable(false);
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (focus.get()) {
                    textField.setEditable(true);
                    textField.getCaret().setVisible(true);
                    focus.set(false);
                }
            }
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                textField.setEditable(false);
            }

            @Override
            public void focusGained(FocusEvent e) {
                focus.set(true);
            }
        });
        textField.setForeground(FOREGROUND);
        textField.setBackground(BACKGROUND);
        textField.setFont(raleway(18));
        textField.setBorder(new BottomBorder(FOREGROUND, 1));
        return textField;
    }

    /**
     * Deletes the user-selected password when he presses the delete key by asking the user, whether he really wants to delete the password,
     * the taking the selected password from {@link ListPasswordPanel#getSelectedPassword()} and deleting it via {@link PassUtils#deletePassword(Password)}
     */
    private void deleteSelectedPassword() {
        if (DIALOG.confirm(LANG.getString("jop.deletePass"))) {
            PassUtils.deletePassword(getSelectedPassword());
            PassFrame.savePasswords();
        }
    }

    /**
     * A custom panel that has a {@link Password} parameter
     */
    private static class PasswordPanel extends JPanel {
        Password password;
        boolean selected;

        public PasswordPanel(LayoutManager layout, Password item) {
            super(layout);
            this.password = item;
        }

        public Password getPassword() {
            return password;
        }

        public void setPassword(Password password) {
            this.password = password;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            setBorder(BorderFactory.createLineBorder(selected ? Color.green : Color.white));
        }
    }
}
