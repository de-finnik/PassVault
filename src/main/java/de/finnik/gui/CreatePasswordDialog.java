package de.finnik.gui;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CreatePasswordDialog extends JDialog {
    private final Consumer<char[]> toDo;
    private final String message;
    private final PassDialog dialog;

    public Color[] colors = new Color[]{new Color(0, 0, 0), new Color(255, 255, 255), new Color(255, 0, 0), new Color(204, 102, 0), new Color(230, 230, 0), new Color(79, 108, 4), new Color(0, 255, 0)};
    public Font font = getFont();

    /**
     * A list of {@link javax.swing.JComponent}s which are generated in {@link CreatePasswordDialog#components()}
     * to be added to the content pane in the right order {@link CreatePasswordDialog#positionComponents(java.util.List)}
     */
    private List<JComponent> components;

    public CreatePasswordDialog(Window owner, String message, Consumer<char[]> toDo, PassDialog dialog) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.toDo = toDo;
        this.message = message;
        this.dialog = dialog;
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

    public void open() {
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
    }

    private void components() {
        JPanel panelToolbar = new JPanel(new BorderLayout());
        panelToolbar.setBackground(getContentPane().getBackground());
        components.add(panelToolbar);

        JLabel lblClose = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.white);
                g.fillRect(0, 0, 30, 30);
            }
        };
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toDo.accept(new char[0]);
                dispose();
            }
        });
        lblClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblClose.setPreferredSize(new Dimension(30, 30));
        panelToolbar.add(lblClose, BorderLayout.EAST);

        JLabel lblPassword = new JLabel(message);
        lblPassword.setForeground(colors[1]);
        lblPassword.setFont(font.deriveFont(15f));
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelToolbar.setMaximumSize(new Dimension(panelToolbar.getMaximumSize().width, lblPassword.getPreferredSize().height));
        panelToolbar.add(lblPassword, BorderLayout.CENTER);

        JTextArea textAreaFeedback = new JTextArea(1, 20);

        JPanel panelStrength = new RoundPanel();

        int width = Math.max(lblPassword.getPreferredSize().width, 200);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(width, 30));
        passwordField.setMaximumSize(passwordField.getPreferredSize());
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Strength strength = new Zxcvbn().measure(new String(passwordField.getPassword()));
                textAreaFeedback.setText(strength.getFeedback().getWarning());
                int index = passwordField.getPassword().length > 0 ? strength.getScore() + 2 : 0;
                fadeBackgroundTo(colors[index], panelStrength);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Consumer<Boolean> accept = b -> {
                        if (b) {
                            toDo.accept(passwordField.getPassword());
                            dispose();
                        }
                    };
                    boolean weak = new Zxcvbn().measure(new String(passwordField.getPassword())).getScore() <= 2;
                    accept.accept(!weak);
                    if (weak) {
                        System.out.println("weak");
                        dialog.confirm(getOwner(), "Sure?", accept);
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

    public static class RoundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
        }
    }
}
