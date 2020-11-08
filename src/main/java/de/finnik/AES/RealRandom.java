package de.finnik.AES;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates a dialog where the user has to type some keys in order to get a real random seed
 */
public class RealRandom {
    /**
     * Displays the dialog where the user has to type some keys in order to get a real random seed
     *
     * @param owner   The {@link Window} object that owns the dialog
     * @param message The message to be displayed inside the dialog
     * @return The user-generated seed
     */
    public static long seedWithUserInput(Window owner, String message) {
        JDialog dialog = new JDialog(owner);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);
        dialog.setUndecorated(true);
        JPanel contentPane = new JPanel();
        dialog.setContentPane(contentPane);
        contentPane.add(new JLabel(message), BorderLayout.CENTER);
        contentPane.setBorder(BorderFactory.createEmptyBorder());
        contentPane.setBackground(Color.white);

        AtomicLong seed = new AtomicLong(-1);

        dialog.addKeyListener(new KeyAdapter() {
            final StringBuilder input = new StringBuilder();

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                input.append(e.getKeyChar());
                if (input.length() == 16) {
                    seed.set(getSeedFromInput(input.toString()));
                    dialog.dispose();
                }
            }
        });
        dialog.setSize(contentPane.getPreferredSize());
        dialog.setLocationRelativeTo(null);

        dialog.setVisible(true);
        return seed.get();
    }

    /**
     * Creates a random seed out of a given {@link String}
     *
     * @param input The input string
     * @return The random seed
     */
    private static long getSeedFromInput(String input) {
        List<Integer> numbers = new ArrayList<>();
        for (char c : input.toCharArray()) {
            numbers.add((int) c);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numbers.size(); i += 2) {
            stringBuilder.append(numbers.get(i) ^ numbers.get(i + 1));
        }
        while (true) {
            try {
                return Long.parseLong(stringBuilder.toString()) ^ System.nanoTime();
            } catch (NumberFormatException e) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        }
    }
}
