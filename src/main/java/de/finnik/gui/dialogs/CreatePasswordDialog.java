package de.finnik.gui.dialogs;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import de.finnik.passvault.PassProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static de.finnik.gui.Var.HAND_CURSOR;
import static de.finnik.gui.Var.LANG;

/**
 * Lets the user create a password, shows the strength of the inputted password and informs the user about possible weaknesses of his new password
 */
public class CreatePasswordDialog extends JDialog {
    private final String message;
    private final Function<Window, Boolean> weak;
    private final Map<String, BufferedImage> images;

    public Color[] colors = new Color[]{new Color(0, 0, 0), new Color(255, 255, 255), new Color(255, 0, 0), new Color(255, 60, 0), new Color(255, 191, 0), new Color(35, 136, 35), new Color(0, 112, 0)};
    public Font font = getFont();
    char[] password;
    /**
     * A list of {@link javax.swing.JComponent}s which are generated in {@link CreatePasswordDialog#components()}
     * to be added to the content pane in the right order {@link CreatePasswordDialog#positionComponents(java.util.List)}
     */
    private List<JComponent> components;

    /**
     * Initializes this dialog.
     * <p>
     * Warning: To make the dialog visible, you have to call {@link CreatePasswordDialog#open()}
     *
     * @param owner   The window that owns this dialog
     * @param message The message that will be displayed
     * @param images  A map of images (Must contain keys: CLOSE, HIDE, SHOW)
     * @param weak    A function that will be called when the password is too weak
     */
    public CreatePasswordDialog(Window owner, String message, Map<String, BufferedImage> images, Function<Window, Boolean> weak) {
        super(owner);
        setModalityType(ModalityType.APPLICATION_MODAL);
        this.message = message;
        this.images = images;
        this.weak = weak;
        if (!images.containsKey("CLOSE") ||
                !images.containsKey("HIDE") ||
                !images.containsKey("SHOW")) {
            throw new IllegalArgumentException("Missing images in map!");
        }

        addWindowListener(new WindowAdapter() {
            /*
            This listener makes sure that the dialog will be disposed when it's activated after the user was inactive
             */
            boolean inactive = false;

            @Override
            public void windowActivated(WindowEvent e) {
                if (inactive) {
                    dispose();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                inactive = true;
            }
        });
    }

    /**
     * Fades the background color of the given component to a given color
     *
     * @param to The target color
     * @param c  The component to be colored
     */
    private static void fadeStrengthTo(Color to, Component c) {
        AtomicInteger r = new AtomicInteger(c.getBackground().getRed());
        AtomicInteger g = new AtomicInteger(c.getBackground().getGreen());
        AtomicInteger b = new AtomicInteger(c.getBackground().getBlue());
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            if (to.getRed() > r.get()) {
                r.getAndIncrement();
            } else if (to.getRed() < r.get()) {
                r.getAndDecrement();
            }
            if (to.getGreen() > g.get()) {
                g.getAndIncrement();
            } else if (to.getGreen() < g.get()) {
                g.getAndDecrement();
            }
            if (to.getBlue() > b.get()) {
                b.getAndIncrement();
            } else if (to.getBlue() < b.get()) {
                b.getAndDecrement();
            }
            Color now = new Color(r.get(), g.get(), b.get());
            c.setBackground(now);
            if (now.equals(to)) {
                service.shutdown();
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates and opens the dialog and returns the password
     *
     * @return The inputted password
     */
    public char[] open() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createLineBorder(colors[1]));
        contentPane.setBackground(colors[0]);
        setContentPane(contentPane);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        components = new ArrayList<>();
        components();
        positionComponents(components);

        adjustSizeAndCenter();
        setVisible(true);
        return password;
    }

    /**
     * Generates the components
     */
    private void components() {
        JPanel panelToolbar = new JPanel(new BorderLayout());
        panelToolbar.setBackground(getContentPane().getBackground());
        components.add(panelToolbar);

        JLabel lblClose = new JLabel();
        lblClose.setIcon(new ImageIcon(images.get("CLOSE")));
        lblClose.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                password = new char[0];
                dispose();
            }
        });
        lblClose.setCursor(HAND_CURSOR);
        lblClose.setPreferredSize(new Dimension(30, 30));
        panelToolbar.add(lblClose, BorderLayout.EAST);

        JLabel lblPassword = new JLabel(message);
        lblPassword.setForeground(colors[1]);
        lblPassword.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        lblPassword.setFont(font.deriveFont(15f));
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelToolbar.add(lblPassword, BorderLayout.CENTER);
        panelToolbar.setMaximumSize(new Dimension(panelToolbar.getMaximumSize().width, lblPassword.getPreferredSize().height));

        JTextArea textAreaFeedback = new JTextArea(1, 20);

        JPanel panelStrength = new RoundPanel();

        JButton btnSubmit = new JButton(LANG.getString("compare.btn.finish"));

        // The coordinates of the hide/show password button
        final Rectangle[] rectangle = {null};
        // Password is (in)visible
        AtomicBoolean show = new AtomicBoolean(false);

        int width = Math.max(lblPassword.getPreferredSize().width, 200);

        JPasswordField passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Paints the image to hide/show the password
                BufferedImage img = images.get(show.get() ? "HIDE" : "SHOW");
                int x = getWidth() - img.getWidth() - 5;
                int y = (getHeight() - img.getHeight()) / 2;
                g.drawImage(img, x, y, null);
                // Stores the images' coordinates
                rectangle[0] = new Rectangle(x, y, img.getWidth(), img.getHeight());
            }
        };
        passwordField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Checks whether user clicked on the hide/show image
                if (e.getX() >= rectangle[0].x && e.getX() <= rectangle[0].x + rectangle[0].width
                        && e.getY() >= rectangle[0].y && e.getY() <= rectangle[0].y + rectangle[0].height) {
                    show.set(!show.get());
                    passwordField.setEchoChar(show.get() ? (char) 0 : new JPasswordField().getEchoChar());
                    passwordField.repaint();
                }
            }
        });
        passwordField.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                // Checks whether user hovers over the hide/show image
                if (e.getX() >= rectangle[0].x && e.getX() <= rectangle[0].x + rectangle[0].width
                        && e.getY() >= rectangle[0].y && e.getY() <= rectangle[0].y + rectangle[0].height) {
                    passwordField.setCursor(HAND_CURSOR);
                } else {
                    passwordField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            }
        });
        passwordField.setPreferredSize(new Dimension(width, 30));
        passwordField.setMaximumSize(passwordField.getPreferredSize());
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Updates the password's strength and gives the user some feedback
                Strength strength = new Zxcvbn().measure(new String(passwordField.getPassword()));
                textAreaFeedback.setText(strength.getFeedback().withResourceBundle(ResourceBundle.getBundle("zxcvbn", new Locale(PassProperty.LANG.getValue()))).getWarning());
                int index = passwordField.getPassword().length > 0 ? strength.getScore() + 2 : 0;
                fadeStrengthTo(colors[index], panelStrength);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSubmit.doClick();
                }
            }
        });
        components.add(passwordField);

        panelStrength.setMaximumSize(new Dimension(width, 10));
        panelStrength.setPreferredSize(panelStrength.getMaximumSize());
        panelStrength.setBackground(colors[0]);
        components.add(panelStrength);

        textAreaFeedback.setFont(font.deriveFont(13f));
        textAreaFeedback.setLineWrap(true);
        textAreaFeedback.setWrapStyleWord(true);
        textAreaFeedback.setMaximumSize(new Dimension(width, 500));
        textAreaFeedback.setForeground(colors[1]);
        textAreaFeedback.setBackground(colors[0]);
        textAreaFeedback.setEditable(false);
        components.add(textAreaFeedback);

        btnSubmit.setForeground(colors[1]);
        btnSubmit.setBackground(colors[0]);
        btnSubmit.setFont(font.deriveFont(15f));
        btnSubmit.addActionListener(a -> {
            if (new Zxcvbn().measure(new String(passwordField.getPassword())).getScore() > 2 || passwordField.getPassword().length == 0 || weak.apply(CreatePasswordDialog.this)) {
                password = passwordField.getPassword();
                dispose();
            } else {
                passwordField.requestFocus();
            }
        });
        components.add(btnSubmit);
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
        setSize(new Dimension((getContentPane().getLayout().preferredLayoutSize(getContentPane())).width + 20, (getContentPane().getLayout().preferredLayoutSize(getContentPane())).height + 60));
        setLocationRelativeTo(null);
    }

    /**
     * A panel whose only purpose is to display its background color inside a rectangle with rounded corners
     */
    private static class RoundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getParent().getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getBackground());
            int arc = 5;
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
    }
}
