package de.finnik.AES;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RealRandom {
    public void seedWithUserInput(String message, Consumer<Long> toDo) {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        JPanel contentPane = new JPanel();
        frame.setContentPane(contentPane);
        contentPane.add(new JLabel(message), BorderLayout.CENTER);
        contentPane.setBorder(BorderFactory.createEmptyBorder());
        contentPane.setBackground(Color.white);
        frame.addKeyListener(new KeyAdapter() {
            final StringBuilder input = new StringBuilder();

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                input.append(e.getKeyChar());
                if (input.length() == 16) {
                    frame.dispose();
                    toDo.accept(getSeedFromInput(input.toString()));
                }
            }
        });
        frame.setVisible(true);
        frame.setSize(contentPane.getPreferredSize());
        frame.setLocationRelativeTo(null);
    }

    public long getSeedFromInput(String input) {
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
