package de.finnik.gui;

import de.finnik.passvault.utils.PassUtils;

import javax.swing.*;
import java.awt.event.ActionListener;

import static de.finnik.gui.Var.BACKGROUND;
import static de.finnik.gui.Var.FOREGROUND;

/**
 * Creates a popup
 */
public class PopUp extends JPopupMenu {

    /**
     * Initializes the popup
     *
     * @param items All {@link PopUpItem}s that should be displayed inside this PopUp
     */
    public PopUp(PopUpItem... items) {
        for (PopUpItem item : items) {
            JMenuItem menuItem = new JMenuItem(item.getMessage());
            menuItem.addActionListener(item.getListener());
            add(menuItem);
        }
    }

    /**
     * A simple JavaBean with two attributes: a message and a ActionListener
     */
    public static class PopUpItem {
        private final String message;
        private final ActionListener listener;

        public PopUpItem(String name, ActionListener listener) {
            this.message = name;
            this.listener = listener;
        }

        public String getMessage() {
            return message;
        }

        public ActionListener getListener() {
            return listener;
        }
    }

    /**
     * A child of {@link PopUp} styled in order to match PassVault's style
     */
    public static class PassPopUp extends PopUp {
        public PassPopUp(PopUpItem... items) {
            super(items);
            setBorder(BorderFactory.createLineBorder(FOREGROUND));
            PassUtils.GUIUtils.doForAllComponents(this, menuItem -> {
                menuItem.setForeground(FOREGROUND);
                menuItem.setBackground(BACKGROUND);
            }, JMenuItem.class);
        }
    }
}
