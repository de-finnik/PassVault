package de.finnik.gui.dialogs;

import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.PassUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.finnik.gui.Var.*;

/**
 * Let the user select which version of each password he would like to keep after loading e.g. a backup of his passwords from a file.
 */
public class CompareDialog extends JDialog {
    JPanel panelLocalElements;
    JPanel panelBackupElements;

    List<PassComp> elements;

    /**
     * The list of passwords that the user selects
     */
    List<Password> chosen;

    boolean closed;

    /**
     * Creates but not displays the dialog. To make it visible, call {@link CompareDialog#open()}
     *
     * @param owner  The parent window
     * @param local  A list of {@link Password} objects
     * @param backup Another list of {@link Password} objects
     */
    public CompareDialog(Window owner, List<Password> local, List<Password> backup) {
        super(owner, ModalityType.APPLICATION_MODAL);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBackground(BACKGROUND);
        contentPane.setBorder(BorderFactory.createLineBorder(Color.white));
        setContentPane(contentPane);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        elements = new ArrayList<>();
        chosen = Utils.findNonDuplicatePasswords(local, backup);

        components();
        findDuplicates(local, backup);
        textComponents();

        PassUtils.GUIUtils.doForAllComponents(contentPane, component -> {
            component.setBackground(BACKGROUND);
            component.setForeground(FOREGROUND);
        }, JPanel.class, JButton.class);

        PassUtils.GUIUtils.doForAllComponents(contentPane, label -> {
            label.setForeground(FOREGROUND);
            label.setBackground(BACKGROUND);
            label.setFont(raleway(15));
        }, JLabel.class, JTextArea.class);

        setSize(getPreferredSize().width, getPreferredSize().height + 100);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            /*
            This listener makes sure that the dialog will be disposed when it's activated after the user was inactive
             */
            boolean inactive = false;

            @Override
            public void windowActivated(WindowEvent e) {
                if (inactive)
                    dispose();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                inactive = true;
            }
        });
        closed = false;
    }

    /**
     * Finds duplicate passwords that doesn't have the same content via {@link Utils#findDuplicateIDs(List, List)},
     * adds the non-corresponding passwords to a list that the user can pick the right version from.
     *
     * @param local  One source
     * @param backup The other source
     */
    private void findDuplicates(List<Password> local, List<Password> backup) {
        List<String> duplicateIDs = Utils.findDuplicateIDs(local, backup);

        Utils.convertDuplicatesToPassComp(duplicateIDs
                .stream()
                .map(id -> Utils.getPasswordForID(id, local))
                .collect(Collectors.toList()), elements, panelLocalElements);

        Utils.convertDuplicatesToPassComp(duplicateIDs
                .stream()
                .map(id -> Utils.getPasswordForID(id, backup))
                .collect(Collectors.toList()), elements, panelBackupElements);

        // Highlight newest version of each password
        duplicateIDs.forEach(id -> Utils.highlightNewest(elements, id));
    }

    /**
     * Creates the main components
     */
    private void components() {
        JPanel panelToolbar = new JPanel(new BorderLayout());
        panelToolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        getContentPane().add(panelToolbar);

        JTextArea textAreaTitle = new JTextArea(LANG.getString("compare.textArea.title"));
        textAreaTitle.setLineWrap(true);
        textAreaTitle.setWrapStyleWord(true);
        textAreaTitle.setEditable(false);
        panelToolbar.add(textAreaTitle, BorderLayout.CENTER);

        JLabel lblClose = new JLabel();
        lblClose.setIcon(new ImageIcon(CLOSE));
        lblClose.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                elements = null;
                dispose();
            }
        });
        lblClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblClose.setPreferredSize(new Dimension(30, 30));
        panelToolbar.add(lblClose, BorderLayout.EAST);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(panel);

        JPanel panelLocal = new JPanel(new BorderLayout());
        JLabel local = new JLabel("Local");
        panelLocal.add(local, BorderLayout.NORTH);
        panel.add(panelLocal, BorderLayout.WEST);

        JPanel panelBackup = new JPanel(new BorderLayout());
        JLabel backup = new JLabel("Backup");
        panelBackup.add(backup, BorderLayout.NORTH);
        panel.add(panelBackup, BorderLayout.EAST);

        GridLayout gridLayout = new GridLayout(0, 1);
        gridLayout.setVgap(10);

        panelLocalElements = new JPanel();
        panelLocalElements.setLayout(gridLayout);
        panelLocalElements.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        panelBackupElements = new JPanel();
        panelBackupElements.setLayout(gridLayout);

        panelLocal.add(panelLocalElements, BorderLayout.CENTER);

        panelBackup.add(panelBackupElements, BorderLayout.CENTER);

        JButton btnFinish = new JButton(LANG.getString("compare.btn.finish"));
        btnFinish.addActionListener(a -> dispose());

        JPanel panelFinish = new JPanel();
        panelFinish.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelFinish.add(btnFinish);
        getContentPane().add(panelFinish);
    }

    /**
     * Makes the dialog visible and returns the chosen passwords
     *
     * @return The chosen passwords
     */
    public List<Password> open() {
        if (elements.size() > 0) {
            setVisible(true);
            if (elements == null)
                return null;
            elements.stream()
                    .filter(PassComp::isSelected)
                    .map(PassComp::getPassword)
                    .forEach(e -> {
                        e.updateModified();
                        chosen.add(e);
                    });
        }
        return chosen;
    }

    private static class Utils {
        /**
         * Finds a {@link Password} with a given id within a list of  {@link Password}s
         * If no passwords is found, you'll get {@code null}
         *
         * @param id        The id that should be found
         * @param passwords The list of {@link Password}s
         * @return The first password with the matching id or {@code null}
         */
        public static Password getPasswordForID(String id, List<Password> passwords) {
            return passwords.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
        }

        /**
         * Returns a list of ids whose passwords have different versions within the two given lists
         *
         * @param one A list of {@link Password} objects
         * @param two Another list of {@link Password} objects
         * @return A list of duplicate password ids
         */
        public static List<String> findDuplicateIDs(List<Password> one, List<Password> two) {
            List<Password> all = new ArrayList<>();
            all.addAll(one);
            all.addAll(two);

            List<String> ids = all.stream().map(Password::id).collect(Collectors.toList());

            return ids.stream()
                    .filter(id -> Collections.frequency(ids, id) == 2)
                    // Remove duplicates that have the same content
                    .filter(id -> !Password.equalsInformation(getPasswordForID(id, one), getPasswordForID(id, two)))
                    .distinct()
                    .collect(Collectors.toList());
        }

        /**
         * Takes two lists of password objects as input and returns every password that has just one version within the two lists
         *
         * @param one A list of {@link Password} objects
         * @param two Another list of {@link Password} objects
         * @return A list of non-duplicate passwords
         */
        public static List<Password> findNonDuplicatePasswords(List<Password> one, List<Password> two) {
            List<Password> all = new ArrayList<>();
            all.addAll(one);
            all.addAll(two);

            List<String> duplicateIDs = findDuplicateIDs(one, two);
            return all.stream()
                    .map(Password::id)
                    .filter(id -> !duplicateIDs.contains(id))
                    .distinct()
                    .map(id -> getPasswordForID(id, all))
                    .collect(Collectors.toList());
        }

        /**
         * Takes a list of {@link Password} objects, a list of {@link PassComp} objects and a {@link JPanel} as input
         * and creates new {@link PassComp} objects that will be added to the existing list. The {@link JPanel} object contained in each new {@link PassComp} object
         * will be appended to the given {@link JPanel} container.
         *
         * @param duplicatePasswords A list of {@link Password} objects
         * @param elements           A list of {@link PassComp} objects
         * @param container          The container to add the new {@link JPanel}s to
         */
        public static void convertDuplicatesToPassComp(List<Password> duplicatePasswords, List<PassComp> elements, JPanel container) {
            List<String> values = new ArrayList<>();
            duplicatePasswords.stream().map(Password::getValues).forEach(v -> v.forEach(values::add));
            int width = Math.max(200, values
                    .stream()
                    .mapToInt(string -> container.getFontMetrics(raleway(15)).stringWidth(string) + 20)
                    .max()
                    .orElse(0));
            for (Password password : duplicatePasswords) {
                JPanel panel = new JPanel();
                panel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                panel.setLayout(new GridLayout(0, 1));

                if (!password.isEmpty()) {
                    password.getValues().forEach(v -> panel.add(new JLabel(v)));
                } else {
                    JLabel trash = new JLabel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.drawImage(TRASH, (getWidth() - TRASH.getWidth()) / 2, (getHeight() - TRASH.getHeight()) / 2, null);
                        }
                    };
                    panel.add(trash);
                }

                PassComp passComp = new PassComp(password, panel);
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        passComp.select(elements);
                    }
                });

                Arrays.stream(panel.getComponents()).filter(c -> c.getClass() == JLabel.class)
                        .forEach(lbl -> ((JLabel) lbl).setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0)));

                elements.add(passComp);
                panel.setPreferredSize(new Dimension(width, 100));
                container.add(panel);
            }
        }

        /**
         * Takes a list of {@link PassComp} objects and a password id as input, finds the latest edited version of this password
         * and highlights its JPanel
         *
         * @param elements A list of all {@link PassComp} objects
         * @param id       The id to highlight the latest version
         */
        public static void highlightNewest(List<PassComp> elements, String id) {
            elements.stream()
                    .filter(comp -> comp.getId().equals(id))
                    .max(Comparator.comparingLong(comp -> comp.getPassword().lastModified()))
                    .orElseThrow(RuntimeException::new)
                    .select(elements);
        }
    }

    /**
     * A helper class to handle the passwords from {@link CompareDialog}
     */
    public static class PassComp {
        private final Password password;
        private final JPanel panel;
        private boolean isSelected;

        public PassComp(Password password, JPanel panel) {
            this.password = password;
            this.panel = panel;
            isSelected = false;
        }

        private static void deselect(List<PassComp> comps, PassComp select) {
            comps.stream()
                    .filter(comp -> comp.getId().equals(select.getId()))
                    .forEach(comp -> comp.setSelected(false));
        }

        private static void paintSelected(List<PassComp> comps) {
            comps.forEach(comp -> comp.getPanel().setBorder(BorderFactory.createLineBorder(comp.isSelected ? Color.green : Color.white)));
        }

        /**
         * Selects this password by deselecting all passwords with the same id in the given list,
         * then selecting itself and then repainting every {@link PassComp} in the given list
         *
         * @param comps A list of {@link PassComp} objects to handle
         */
        public void select(List<PassComp> comps) {
            deselect(comps, this);
            setSelected(true);
            paintSelected(comps);
        }

        public Password getPassword() {
            return password;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getId() {
            return password.id();
        }

        public JPanel getPanel() {
            return panel;
        }
    }
}
