package de.finnik.gui.dialogs;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import de.finnik.passvault.PassProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class CreatePasswordDialog extends JDialog {
    private final String message;
    private final Supplier<Boolean> weak;
    private final Map<String, BufferedImage> images;

    public Color[] colors = new Color[]{new Color(0, 0, 0), new Color(255, 255, 255), new Color(255, 0, 0), new Color(255, 60, 0), new Color(255, 191, 0), new Color(35, 136, 35), new Color(0, 112, 0)};
    public Font font = getFont();
    char[] password;
    /**
     * A list of {@link javax.swing.JComponent}s which are generated in {@link CreatePasswordDialog#components()}
     * to be added to the content pane in the right order {@link CreatePasswordDialog#positionComponents(java.util.List)}
     */
    private List<JComponent> components;

    public CreatePasswordDialog(Window owner, String message, Map<String, BufferedImage> images, Supplier<Boolean> weak) {
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
    }

    private static void fadeBackgroundTo(Color to, Component c) {
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

    public char[] open() {
        setContentPane(new JPanel());
        ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(colors[1]));
        getContentPane().setBackground(colors[0]);

        BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        getContentPane().setLayout(boxLayout);
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
        lblClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblClose.setPreferredSize(new Dimension(30, 30));
        panelToolbar.add(lblClose, BorderLayout.EAST);

        JLabel lblPassword = new JLabel(message);
        lblPassword.setForeground(colors[1]);
        lblPassword.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        lblPassword.setFont(font.deriveFont(15f));
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelToolbar.setMaximumSize(new Dimension(panelToolbar.getMaximumSize().width, lblPassword.getPreferredSize().height));
        panelToolbar.add(lblPassword, BorderLayout.CENTER);

        JTextArea textAreaFeedback = new JTextArea(1, 20);

        JPanel panelStrength = new RoundPanel();

        int width = Math.max(lblPassword.getPreferredSize().width, 200);
        final Rectangle[] rectangle = {null};
        AtomicBoolean show = new AtomicBoolean(false);

        JPasswordField passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage img = images.get(show.get() ? "HIDE" : "SHOW");
                int x = getWidth() - img.getWidth() - 5;
                int y = (getHeight() - img.getHeight()) / 2;
                g.drawImage(img, x, y, null);
                rectangle[0] = new Rectangle(x, y, img.getWidth(), img.getHeight());
            }
        };
        passwordField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
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
                if (e.getX() >= rectangle[0].x && e.getX() <= rectangle[0].x + rectangle[0].width
                        && e.getY() >= rectangle[0].y && e.getY() <= rectangle[0].y + rectangle[0].height) {
                    passwordField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                Strength strength = new Zxcvbn().measure(new String(passwordField.getPassword()));
                textAreaFeedback.setText(strength.getFeedback().withResourceBundle(ResourceBundle.getBundle("zxcvbn", new Locale(PassProperty.LANG.getValue()))).getWarning());
                int index = passwordField.getPassword().length > 0 ? strength.getScore() + 2 : 0;
                fadeBackgroundTo(colors[index], panelStrength);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (new Zxcvbn().measure(new String(passwordField.getPassword())).getScore() > 2 || passwordField.getPassword().length == 0 || weak.get()) {
                        password = passwordField.getPassword();
                        dispose();
                    } else {
                        passwordField.requestFocus();
                    }
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

    private void adjustSizeAndCenter() {
        setSize(new Dimension((getContentPane().getLayout().preferredLayoutSize(getContentPane())).width + 20, (getContentPane().getLayout().preferredLayoutSize(getContentPane())).height + 60));
        setLocationRelativeTo(null);
    }

    private static class RoundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getParent().getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
        }
    }
}
