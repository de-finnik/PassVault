package de.finnik.gui;

import de.finnik.passvault.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;

import static de.finnik.gui.Var.*;

/**
 * Observing your passwords via inserting a parameter
 */
class PassBankPanel extends JPanel {

    private static JTextField tfSearch;
    private static DefaultTableModel tableModelPassBank;
    private static JButton btnShowPass;
    private static JLabel lblCopy;
    /**
     * Creates the panel
     */
    PassBankPanel() {
        setBackground(BACKGROUND);
        setLayout(null);

        components();
        textComponents();

        Arrays.stream(getMatchingComponents("passBank.lbl", "passBank.table", "passBank.tf"))
                .forEach(c -> c.setFont(raleway(13)));
    }

    /**
     * Checks via {@link PassBankPanel#showAll()} whether {@link de.finnik.gui.PassBankPanel#tableModelPassBank} should display all passwords
     * or just the ones matching to the input from {@link de.finnik.gui.PassBankPanel#tfSearch}
     *
     * @return All passwords to be currently displayed in {@link de.finnik.gui.PassBankPanel#tableModelPassBank}
     */
    private static ArrayList<Password> getAllMatchingPasswords() {
        return (ArrayList<Password>) PassFrame.passwordList.stream()
                .filter(pass -> pass.getValues()
                        .anyMatch(value -> showAll() || (tfSearch.getText().length() >= 3 && value.toLowerCase().contains(tfSearch.getText().toLowerCase()))))
                .collect(Collectors.toList());
    }

    /**
     * Updates {@link de.finnik.gui.PassBankPanel#tableModelPassBank} via adding all passwords from {@link PassBankPanel#getAllMatchingPasswords()} to {@link de.finnik.gui.PassBankPanel#tableModelPassBank}
     */
    static void updateTableModel() {
        tableModelPassBank.setRowCount(0);
        for (Password password : getAllMatchingPasswords()) {
            tableModelPassBank.addRow(new String[]{password.getPass(), password.getSite(), password.getUser(), password.getOther()});
        }
    }

    /**
     * Checks whether {@link de.finnik.gui.PassBankPanel#tableModelPassBank} should display all passwords or just the ones that match with the user input
     *
     * @return A boolean (true=All passwords should be displayed; false=passwords matching to user input should be displayed)
     */
    private static boolean showAll() {
        return btnShowPass.getBackground().equals(BACKGROUND);
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    lblCopy.getMouseListeners()[0].mouseClicked(null);
                }
            }
        });
        add(tfSearch, "passBank.tf.search");

        JScrollPane scrollPanePassBank = new JScrollPane();
        JTable tablePassBank = new JTable();
        COMPONENTS.put("passBank.table", tablePassBank);


        scrollPanePassBank.setViewportView(tablePassBank);
        scrollPanePassBank.getViewport().setBackground(BACKGROUND);
        scrollPanePassBank.setBorder(BorderFactory.createEmptyBorder());
        scrollPanePassBank.setBounds(0, 70, 350, 280);

        tablePassBank.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePassBank.setShowGrid(false);
        tablePassBank.setAutoCreateRowSorter(true);
        tablePassBank.setFillsViewportHeight(true);

        tablePassBank.getTableHeader().setBackground(BACKGROUND);
        tablePassBank.getTableHeader().setForeground(FOREGROUND);
        tablePassBank.getTableHeader().setResizingAllowed(false);
        tablePassBank.getTableHeader().setReorderingAllowed(false);


        tablePassBank.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(FOREGROUND);
                setForeground(BACKGROUND);
                if (hasFocus) {
                    setBorder(BorderFactory.createLineBorder(BACKGROUND));
                }
                return this;
            }
        });

        tableModelPassBank = new DefaultTableModel(new String[0][4], LANG.getProperty("passBank.table.header").split("#"));
        tableModelPassBank.addTableModelListener(e -> {
            /* Lets you edit a password
             * Note: Editing a password so that it would has no information, will delete it after confirming
             */
            if (e.getType() == TableModelEvent.UPDATE && tablePassBank.getSelectedRow() >= 0) {
                Password password = getAllMatchingPasswords().get(tablePassBank.getSelectedRow());

                // Checks whether the password would be empty after the edit
                if (((String) tableModelPassBank.getValueAt(e.getFirstRow(), e.getColumn())).length() == 0 && password.emptyParameters() == 3 && password.isEmpty(e.getColumn())) {
                    DIALOG.confirm(FRAME, LANG.getProperty("jop.deletePass"), confirm -> {
                        if (confirm) {
                            PassFrame.passwordList.remove(password);
                            PassFrame.savePasswords();
                        }
                        updateTableModel();
                    });
                    return;
                }

                // Edits the password
                switch (e.getColumn()) {
                    case 0:
                        password.setPass((String) tableModelPassBank.getValueAt(e.getFirstRow(), e.getColumn()));
                        break;
                    case 1:
                        password.setSite((String) tableModelPassBank.getValueAt(e.getFirstRow(), e.getColumn()));
                        break;
                    case 2:
                        password.setUser((String) tableModelPassBank.getValueAt(e.getFirstRow(), e.getColumn()));
                        break;
                    case 3:
                        password.setOther((String) tableModelPassBank.getValueAt(e.getFirstRow(), e.getColumn()));
                        break;

                }
                LOG.info("Edited password: {}!", Password.log(password));
                PassFrame.savePasswords();
            }
        });
        tablePassBank.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                // Lets you delete a password via pressing delete
                if (e.getKeyCode() == KeyEvent.VK_DELETE && tablePassBank.getSelectedRow() >= 0) {
                    DIALOG.confirm(FRAME, LANG.getProperty("jop.deletePass"), confirm -> {
                        if (confirm) {
                            int row = tablePassBank.getSelectedRow();
                            Password password = getAllMatchingPasswords().get(row);
                            PassFrame.passwordList.remove(password);
                            tableModelPassBank.removeRow(row);
                            LOG.info("Deleted password: {}!", Password.log(password));
                            PassFrame.savePasswords();
                        }
                    });
                }
            }
        });
        tablePassBank.setModel(tableModelPassBank);
        add(scrollPanePassBank);

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
                    int row = tablePassBank.getRowCount() == 1 ? 0 : tablePassBank.getSelectedRow();
                    Password password = getAllMatchingPasswords().get(row);
                    Utils.copyToClipboard(password.getPass());
                    LOG.info("Copied password to clipboard: {}!", Password.log(password));
                } catch (IndexOutOfBoundsException ex) {
                    DIALOG.message(null, LANG.getProperty("passBank.jop.noEntrySelected"));
                }
            }
        });
        add(lblCopy, "passBank.lbl.copy");
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
        add(c);
    }
}
