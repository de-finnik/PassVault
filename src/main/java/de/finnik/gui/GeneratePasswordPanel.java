package de.finnik.gui;

import de.finnik.passvault.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static de.finnik.gui.Var.*;

/**
 * Generate a password by selecting the characters to use and the password length.
 */
public class GeneratePasswordPanel extends JPanel {

    /**
     * Creates the panel
     */
    public GeneratePasswordPanel() {
        setBackground(BACKGROUND);
        setLayout(null);

        components();
        textComponents();

        // Sets the color for all generated components
        PassUtils.GUIUtils.colorComponents(getMatchingComponents("generate"), FOREGROUND, BACKGROUND);

        // Loads the icons for the checkboxes
        Arrays.stream(getMatchingComponents("generate.checkBox"))
                .map(c -> ((JCheckBox) c))
                .forEach(checkBox -> {
                    checkBox.setIcon(new ImageIcon(NOT_SELECTED));
                    checkBox.setSelectedIcon(new ImageIcon(SELECTED));
                });

        // Sets the font for labels and checkboxes
        Arrays.stream(getMatchingComponents("generate.checkBox", "generate.lbl"))
                .forEach(c -> c.setFont(raleway(14)));

        // Sets the font for buttons and textfields
        Arrays.stream(getMatchingComponents("generate.btn", "generate.tf"))
                .forEach(c -> c.setFont(raleway(15)));
    }


    /**
     * Generates the components
     */
    public void components() {
        JLabel lblHeadline = new JLabel();
        lblHeadline.setBounds(0, 0, 300, 30);
        add(lblHeadline, "generate.lbl.headline");

        JSlider sliderLength = new JSlider(5, 30, 12);
        sliderLength.setMajorTickSpacing(5);
        sliderLength.setMinorTickSpacing(1);
        sliderLength.setPaintTicks(true);
        sliderLength.setPaintLabels(true);
        sliderLength.setPaintTrack(false);
        sliderLength.setBounds(0, 40, 300, 50);
        add(sliderLength, "generate.slider.length");

        UIManager.put("CheckBox.focus", BACKGROUND);

        JCheckBox checkBoxBigLetters = new JCheckBox();
        checkBoxBigLetters.setSelected(true);
        checkBoxBigLetters.setBounds(0, 100, 300, 30);
        add(checkBoxBigLetters, "generate.checkBox.bigLetters");

        JCheckBox checkBoxSmallLetters = new JCheckBox();
        checkBoxSmallLetters.setSelected(true);
        checkBoxSmallLetters.setBounds(0, 130, 300, 30);
        add(checkBoxSmallLetters, "generate.checkBox.smallLetters");

        JCheckBox checkBoxNumbers = new JCheckBox();
        checkBoxNumbers.setSelected(true);
        checkBoxNumbers.setBounds(0, 160, 300, 30);
        add(checkBoxNumbers, "generate.checkBox.numbers");

        JCheckBox checkBoxSpecialCharacters = new JCheckBox();
        checkBoxSpecialCharacters.setSelected(false);
        checkBoxSpecialCharacters.setBounds(0, 190, 300, 30);
        add(checkBoxSpecialCharacters, "generate.checkBox.specials");

        JTextField tfPass = new JTextField();

        JButton btnGenerate = new JButton();
        btnGenerate.setBounds(75, 230, 150, 30);
        btnGenerate.addActionListener(action -> {
            String chars = (checkBoxBigLetters.isSelected() ? PasswordGenerator.BIG_LETTERS() : "") +
                    (checkBoxSmallLetters.isSelected() ? PasswordGenerator.SMALL_LETTERS() : "") +
                    (checkBoxNumbers.isSelected() ? PasswordGenerator.NUMBERS() : "") +
                    (checkBoxSpecialCharacters.isSelected() ? PasswordGenerator.SPECIAL_CHARACTERS() : "");
            if (chars.length() > 0) {
                tfPass.setText(PasswordGenerator.generatePassword(chars, sliderLength.getValue()));
                LOG.info("Generated password with length: {} and chars {}! ", sliderLength.getValue(), chars);
            } else {
                DIALOG.message(FRAME, LANG.getProperty("generate.jop.insufficientChars"));
            }
        });
        add(btnGenerate, "generate.btn.generate");

        tfPass.setBounds(0, 270, 300, 40);
        tfPass.setEditable(false);
        add(tfPass, "generate.tf.pass");

        JButton btnSavePass = new JButton();
        btnSavePass.setBounds(25, 320, 250, 30);
        btnSavePass.addActionListener(action -> {
            JDialog savePassDialog = new SavePassDialog(FRAME, tfPass.getText());
            COMPONENTS.put("savePass", savePassDialog);
            savePassDialog.setVisible(true);
        });
        add(btnSavePass, "generate.btn.savePass");
    }

    /**
     * Adds a component with its name to the {@link Var#COMPONENTS} map and adds the component to the panel
     * The method kind of overwrites {@link Container#add(Component)} method in order to handle the components later
     *
     * @param c   The component
     * @param key The component´s matching name
     */
    private void add(Component c, String key) {
        COMPONENTS.put(key, c);
        add(c);
    }
}
