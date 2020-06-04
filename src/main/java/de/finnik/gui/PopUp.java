package de.finnik.gui;

import javax.swing.*;
import java.awt.event.ActionListener;

public class PopUp extends JPopupMenu {
    public PopUp(PopUpItem... items) {
        for (PopUpItem item : items) {
            JMenuItem menuItem = new JMenuItem(item.getName());
            menuItem.addActionListener(item.getListener());
            add(menuItem);
        }
    }

    public static class PopUpItem {
        private final String name;
        private final ActionListener listener;

        public PopUpItem(String name, ActionListener listener) {
            this.name = name;
            this.listener = listener;
        }

        public String getName() {
            return name;
        }

        public ActionListener getListener() {
            return listener;
        }
    }
}
