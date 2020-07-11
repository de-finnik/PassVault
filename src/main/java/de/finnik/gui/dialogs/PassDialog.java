package de.finnik.gui.dialogs;

import javax.swing.*;
import javax.swing.text.*;
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
    private final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    public Window OWNER;

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

        JTextPane textPaneMessage = new JTextPane();
        textPaneMessage.setFont(FONT.deriveFont(13f));
        textPaneMessage.setBackground(BACKGROUND);
        textPaneMessage.setForeground(FOREGROUND);
        textPaneMessage.setEditable(false);
        textPaneMessage.setEditorKit(new CenteredEditorKit());
        try {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
            StyledDocument doc = (StyledDocument) textPaneMessage.getDocument();
            doc.insertString(0, message, attrs);
            doc.setParagraphAttributes(0, doc.getLength() - 1, attrs, false);
        } catch (Exception ex) {
            throw new RuntimeException("Exception while setting message text for JTextPane in PassDialog", ex);
        }
        textPaneMessage.setSize(new Dimension(Math.min(textPaneMessage.getPreferredSize().width, OWNER.getWidth()), 10));
        textPaneMessage.setPreferredSize(new Dimension(textPaneMessage.getSize().width, textPaneMessage.getPreferredSize().height));
        content.add(textPaneMessage, BorderLayout.CENTER);

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

        JTextPane textPaneMessage = new JTextPane();
        textPaneMessage.setText(message);
        textPaneMessage.setFont(FONT.deriveFont(13f));
        textPaneMessage.setBackground(BACKGROUND);
        textPaneMessage.setForeground(FOREGROUND);
        textPaneMessage.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        textPaneMessage.setEditable(false);
        textPaneMessage.setSize(new Dimension(Math.min(textPaneMessage.getPreferredSize().width, OWNER.getWidth()), 10));
        textPaneMessage.setPreferredSize(new Dimension(textPaneMessage.getSize().width, textPaneMessage.getPreferredSize().height));

        panel.add(textPaneMessage, BorderLayout.NORTH);

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
        tfInput.setPreferredSize(new Dimension(Math.max(textPaneMessage.getPreferredSize().width + 100, 250), 25));
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

        JTextPane textPaneMessage = new JTextPane();
        textPaneMessage.setFont(FONT.deriveFont(13f));
        textPaneMessage.setBackground(BACKGROUND);
        textPaneMessage.setForeground(FOREGROUND);
        textPaneMessage.setEditable(false);
        textPaneMessage.setEditorKit(new CenteredEditorKit());
        try {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
            StyledDocument doc = (StyledDocument) textPaneMessage.getDocument();
            doc.insertString(0, message, attrs);
            doc.setParagraphAttributes(0, doc.getLength() - 1, attrs, false);
        } catch (Exception ex) {
            throw new RuntimeException("Exception while setting message text for JTextPane in PassDialog", ex);
        }
        textPaneMessage.setSize(new Dimension(Math.min(textPaneMessage.getPreferredSize().width, OWNER.getWidth()), 10));
        textPaneMessage.setPreferredSize(new Dimension(textPaneMessage.getSize().width, textPaneMessage.getPreferredSize().height));
        content.add(textPaneMessage);

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

    private static class CenteredEditorKit extends StyledEditorKit {
        public ViewFactory getViewFactory() {
            return new StyledViewFactory();
        }

        static class StyledViewFactory implements ViewFactory {
            public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                    switch (kind) {
                        case AbstractDocument.ContentElementName:

                            return new LabelView(elem);
                        case AbstractDocument.ParagraphElementName:
                            return new ParagraphView(elem);
                        case AbstractDocument.SectionElementName:
                            return new BoxView(elem, View.Y_AXIS) {
                                protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {

                                    super.layoutMajorAxis(targetSpan, axis, offsets, spans);
                                    int textBlockHeight = 0;
                                    int offset;
                                    for (int span : spans) {
                                        textBlockHeight = span;
                                    }
                                    offset = (targetSpan - textBlockHeight) / 2;
                                    for (int i = 0; i < offsets.length; i++) {
                                        offsets[i] += offset;
                                    }

                                }
                            };
                        case StyleConstants.ComponentElementName:
                            return new ComponentView(elem);
                        case StyleConstants.IconElementName:

                            return new IconView(elem);
                    }
                }

                return new LabelView(elem);
            }

        }
    }
}
