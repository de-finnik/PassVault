package de.finnik.gui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * With an instance you can show small dialogs for simple actions
 */
public class PassDialog {
    private final BufferedImage CLOSE;
    private final BufferedImage[] IMAGES;
    private final Color FOREGROUND, BACKGROUND;
    private final Font FONT;
    public Window OWNER;

    private final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    /**
     * Predefines settings that will be used later
     *
     * @param foreground The foreground of the dialogs
     * @param background The background of the dialogs
     * @param font       The font that will be used for the dialogs
     * @param close      An image which will be displayed by a button that closes the dialog
     * @param images     An array of images that will be used for icons in the dialogs
     *                   (images[0] in {@link PassDialog#message(String)}, images[1] in {@link PassDialog#input(String)}
     *                   images[2] in {@link PassDialog#confirm(String)}
     *                   and images[3] as the button in {@link PassDialog#confirm(String)} with that the user confirms the dialog
     */
    public PassDialog(Color foreground, Color background, Font font, BufferedImage close, BufferedImage... images) {
        this.CLOSE = close;
        this.IMAGES = images;
        this.FOREGROUND = foreground;
        this.BACKGROUND = background;
        this.FONT = font;
    }

    /**
     * Displays a message dialog which displays a simple message
     *
     * @param message The message which the user should see
     */
    public void message(String message) {
        Toolkit.getDefaultToolkit().beep();

        JDialog dialog = new JDialog(OWNER);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);

        JPanel content = new JPanel();

        dialog.setContentPane(content);
        dialog.setUndecorated(true);

        content.setBackground(BACKGROUND);
        content.setLayout(new BorderLayout(20, 0));
        content.setBorder(BorderFactory.createLineBorder(FOREGROUND, 1));

        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(new ImageIcon(IMAGES[0]));
        lblLogo.setSize(lblLogo.getPreferredSize());
        content.add(lblLogo, BorderLayout.WEST);


        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(FONT.deriveFont(13f));
        lblMessage.setForeground(FOREGROUND);
        content.add(lblMessage, BorderLayout.CENTER);

        JLabel lblClose = new JLabel() {
            @Override
            public void addNotify() {
                super.addNotify();
                requestFocus();
            }
        };
        lblClose.setCursor(HAND_CURSOR);
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });
        lblClose.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_ESCAPE:
                    case KeyEvent.VK_SPACE:
                        lblClose.getMouseListeners()[0].mouseClicked(null);
                }
            }
        });
        lblClose.setIcon(new ImageIcon(CLOSE));
        lblClose.setSize(lblLogo.getPreferredSize());
        lblClose.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        content.add(lblClose, BorderLayout.EAST);

        dialog.setSize(new Dimension(content.getLayout().preferredLayoutSize(content).width + 50, content.getLayout().preferredLayoutSize(content).height + 50));
        dialog.setSize(content.getLayout().preferredLayoutSize(content));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Displays an input dialog which displays a message and lets the user enter something
     *
     * @param message The message which the user should see
     */
    public String input(String message) {
        return input(message, false);
    }

    /**
     * Displays the same input dialog as {@link PassDialog#input(String)} but accepts a boolean to let the user input a password
     *
     * @param message The message which the user should see
     * @param pass    A boolean whether the input is a password
     * @return The user's input
     */
    public String input(String message, boolean pass) {
        JDialog dialog = new JDialog(OWNER);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);

        String[] result = new String[1];

        JPanel content = new JPanel(new FlowLayout());

        dialog.setContentPane(content);
        dialog.setUndecorated(true);

        content.setBackground(BACKGROUND);
        ((FlowLayout) content.getLayout()).setHgap(10);
        content.setBorder(BorderFactory.createLineBorder(FOREGROUND, 1));

        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(new ImageIcon(IMAGES.length >= 2 ? IMAGES[1] : IMAGES[0]));
        lblLogo.setSize(lblLogo.getPreferredSize());
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        content.add(lblLogo, BorderLayout.WEST);

        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        content.add(panel, BorderLayout.CENTER);

        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(FONT.deriveFont(13f));
        lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        lblMessage.setForeground(FOREGROUND);

        panel.add(lblMessage, BorderLayout.NORTH);

        JTextField tfInput = pass ? new JPasswordField() {
            @Override
            public void addNotify() {
                super.addNotify();
                requestFocus();
            }
        } : new JTextField() {
            @Override
            public void addNotify() {
                super.addNotify();
                requestFocus();
            }
        };
        tfInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    result[0] = tfInput.getText();
                    dialog.dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    result[0] = "";
                }
            }
        });
        tfInput.setFont(FONT.deriveFont(pass ? 20f : 15f));
        tfInput.setPreferredSize(new Dimension(Math.max(lblMessage.getPreferredSize().width + 100, 250), 25));
        tfInput.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.add(tfInput, BorderLayout.CENTER);

        JLabel lblYes = new JLabel();
        lblYes.setCursor(HAND_CURSOR);
        lblYes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                result[0] = tfInput.getText();
                dialog.dispose();
            }
        });
        lblYes.setIcon(new ImageIcon(IMAGES[3]));
        content.add(lblYes);

        JLabel lblClose = new JLabel();
        lblClose.setCursor(HAND_CURSOR);
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                result[0] = "";
                dialog.dispose();
            }
        });
        lblClose.setIcon(new ImageIcon(CLOSE));
        lblClose.setSize(lblLogo.getPreferredSize());
        lblClose.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        content.add(lblClose, BorderLayout.EAST);

        dialog.setSize(new Dimension(content.getLayout().preferredLayoutSize(content).width + 50, content.getLayout().preferredLayoutSize(content).height + 50));
        dialog.setSize(content.getLayout().preferredLayoutSize(content));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return result[0];
    }


    /**
     * Displays a confirm dialog which displays a question that the user can answer with yes or no
     *
     * @param message The message which the user should see
     * @return The user's confirmation
     */
    public boolean confirm(String message) {
        JDialog dialog = new JDialog(OWNER);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);

        JPanel content = new JPanel();

        AtomicBoolean result = new AtomicBoolean(false);

        dialog.setContentPane(content);
        dialog.setUndecorated(true);

        content.setBackground(BACKGROUND);
        content.setLayout(new FlowLayout());
        ((FlowLayout) content.getLayout()).setHgap(10);
        content.setBorder(BorderFactory.createLineBorder(FOREGROUND, 1));

        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(new ImageIcon(IMAGES.length >= 3 ? IMAGES[2] : IMAGES[0]));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        content.add(lblLogo);

        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(FONT.deriveFont(13f));
        lblMessage.setPreferredSize(new Dimension(Math.max(lblMessage.getPreferredSize().width + 20, 100), 20));
        lblMessage.setForeground(FOREGROUND);

        content.add(lblMessage);

        JLabel lblYes = new JLabel() {
            @Override
            public void addNotify() {
                super.addNotify();
                requestFocus();
            }
        };
        lblYes.setCursor(HAND_CURSOR);
        lblYes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                result.set(true);
                dialog.dispose();
            }
        });
        lblYes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    result.set(true);
                    dialog.dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    result.set(false);
                    dialog.dispose();
                }
            }
        });
        lblYes.setIcon(new ImageIcon(IMAGES[3]));
        content.add(lblYes);


        JLabel lblClose = new JLabel();
        lblClose.setCursor(HAND_CURSOR);
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                result.set(false);
                dialog.dispose();
            }
        });
        lblClose.setIcon(new ImageIcon(CLOSE));
        lblClose.setSize(lblLogo.getPreferredSize());
        lblClose.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        content.add(lblClose);

        dialog.setSize(new Dimension(content.getLayout().preferredLayoutSize(content).width + 50, content.getLayout().preferredLayoutSize(content).height + 50));
        dialog.setSize(content.getLayout().preferredLayoutSize(content));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return result.get();
    }
}