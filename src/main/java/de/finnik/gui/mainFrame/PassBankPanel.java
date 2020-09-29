package de.finnik.gui.mainFrame;

import de.finnik.gui.Var;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.PassUtils;

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
import java.util.stream.Collectors;

import static de.finnik.gui.Var.*;

/**
 * Observing your passwords via inserting a parameter
 */
public class PassBankPanel extends JPanel {

    private static JTextField tfSearch;
    private static ListPasswordPanel listPasswordPanel;
    private static JButton btnShowPass;
    private static JLabel lblCopy;

    /**
     * Creates the panel
     */
    public PassBankPanel() {
        setBackground(BACKGROUND);
        setLayout(null);

        components();
        textComponents();

        Arrays.stream(getMatchingComponents("passBank.lbl", "passBank.table", "passBank.tf"))
                .forEach(c -> c.setFont(raleway(13)));
    }

    /**
     * Generates the components
     */
    private void components() {
        JLabel lblSearch = new JLabel();
        lblSearch.setForeground(FOREGROUND);
        lblSearch.setBounds(0, 0, 200, 30);
        add(lblSearch, "passBank.lbl.search");

        tfSearch = new JTextField() {
            @Override
            public void addNotify() {
                super.addNotify();
                requestFocus();
            }
        };
        tfSearch.setBorder(BorderFactory.createLineBorder(FOREGROUND));
        tfSearch.setBounds(0, 30, 230, 30);
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateTableModel();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER && getAllMatchingPasswords().size() > 0) {
                    lblCopy.getMouseListeners()[0].mouseClicked(null);
                }
            }
        });
        add(tfSearch, "passBank.tf.search");

        listPasswordPanel = new ListPasswordPanel();
        listPasswordPanel.setBackground(Color.pink);
        listPasswordPanel.setBounds(0, 70, 350, 280);
        add(listPasswordPanel, "passBank.listPasswordPanel");

        btnShowPass = new JButton();
        btnShowPass.setBounds(240, 30, 60, 30);
        btnShowPass.setForeground(BACKGROUND);
        btnShowPass.setBackground(FOREGROUND);
        btnShowPass.setFont(raleway(10).deriveFont(Font.BOLD));
        btnShowPass.addActionListener(action -> {
            btnShowPass.setBackground(btnShowPass.getBackground().equals(BACKGROUND) ? FOREGROUND : BACKGROUND);
            btnShowPass.setForeground(btnShowPass.getForeground().equals(BACKGROUND) ? FOREGROUND : BACKGROUND);

            updateTableModel();
        });
        add(btnShowPass, "passBank.btn.showPass");

        lblCopy = new JLabel();
        lblCopy.setForeground(FOREGROUND);
        lblCopy.setIcon(new ImageIcon(COPY));
        lblCopy.setSize(lblCopy.getPreferredSize());
        lblCopy.setBounds(310, 30, lblCopy.getWidth(), lblCopy.getHeight());
        lblCopy.setCursor(HAND_CURSOR);
        lblCopy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Password password = listPasswordPanel.getSelectedPassword();
                    PassUtils.copyToClipboard(password.getPass());
                    LOG.info(Password.log(password, "Copied password to clipboard"));
                } catch (NullPointerException ex) {
                    DIALOG.message(LANG.getString("passBank.jop.noEntrySelected"));
                } catch (IOException ioException) {
                    LOG.error("Error while checking for hints!");
                }
            }
        });
        add(lblCopy, "passBank.lbl.copy");
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
        add(c);
    }

    /**
     * Checks via {@link PassBankPanel#showAll()} whether {@link PassBankPanel#listPasswordPanel} should display all passwords
     * or just the ones matching to the input from {@link PassBankPanel#tfSearch}
     *
     * @return All passwords to be currently displayed in {@link PassBankPanel#listPasswordPanel}
     */
    private List<Password> getAllMatchingPasswords() {
        if (showAll()) {
            return PassFrame.passwordList.stream().filter(p -> !p.isEmpty()).collect(Collectors.toList());
        } else if (tfSearch.getText().length() >= 3) {
            return PassUtils.getAllMatchingPasswords(tfSearch.getText(), PassFrame.passwordList);
        }
        return new ArrayList<>();
    }

    /**
     * Updates {@link PassBankPanel#listPasswordPanel} via adding all passwords from {@link PassBankPanel#getAllMatchingPasswords()} to {@link PassBankPanel#listPasswordPanel}
     */
    public void updateTableModel() {
        listPasswordPanel.display(getAllMatchingPasswords());
        tfSearch.requestFocus();
    }

    /**
     * Checks whether {@link PassBankPanel#listPasswordPanel} should display all passwords or just the ones that match with the user input
     *
     * @return A boolean (true=All passwords should be displayed; false=passwords matching to user input should be displayed)
     */
    private boolean showAll() {
        return btnShowPass.getBackground().equals(BACKGROUND);
    }
}
