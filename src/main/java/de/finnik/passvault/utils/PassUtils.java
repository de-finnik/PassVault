package de.finnik.passvault.utils;

import de.finnik.gui.PassVault;
import de.finnik.gui.hints.HintBrowser;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.passwords.Password;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.finnik.gui.Var.*;

/**
 * Contains useful methods that concern this application
 */
public class PassUtils {

    public static List<Password> getAllMatchingPasswords(String key, List<Password> passwords) {
        return passwords.stream().filter(pass -> pass.getValues().anyMatch(arg -> arg.toLowerCase().contains(key.toLowerCase()))).collect(Collectors.toList());
    }

    public static void deletePassword(Password password) {
        password.setPass("");
        password.setSite("");
        password.setUser("");
        password.setOther("");
    }

    public static void copyToClipboard(Window owner, String string) throws IOException {
        if (!HINTS.triggerHint("hints.windows.clipboardHistory", t -> {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                if (DIALOG.confirm(owner, LANG.getString(t))) {
                    try {
                        HintBrowser.show(FRAME, t, PassProperty.LANG.getValue());
                    } catch (IOException e) {
                        LOG.error("Error while loading hint {}", t, e);
                    }
                }
            }
        })) {
            Utils.copyToClipboard(string);
        }
    }

    /**
     * Contains useful methods that do stuff with the saved data
     */
    public static class FileUtils {
        /**
         * Loads all available languages from resources/lang directory
         *
         * @return A list of all available languages in format: (en, de...)
         */
        public static List<String> availableLanguages() {
            List<String> languages = new ArrayList<>();
            try {
                for (Locale availableLocale : Locale.getAvailableLocales()) {
                    if (ResourceBundle.getBundle("passvault", availableLocale).getLocale().equals(availableLocale)) {
                        languages.add(availableLocale.getLanguage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return languages;
        }

        /**
         * Loads the texts for a given language pack.
         *
         * @param lang The language to load the texts from
         * @return The properties containing all texts for all components
         */
        public static Properties loadLang(String lang) {
            Properties LANG = new Properties();
            try {
                LANG.load(PassVault.class.getResourceAsStream("/lang/" + lang + ".properties"));
                LOG.info("Loaded properties for language {}!", lang);
            } catch (Exception e) {
                LOG.error("Error while loading language properties from: {}.properties!", lang);
            }
            return LANG;
        }
    }

    /**
     * Contains useful methods that do stuff with the GUI of the application
     */
    public static class GUIUtils {
        /**
         * Filters a map of components with their matching names after string patterns:
         * The name of the component has to contain at least one string pattern to be returned
         *
         * @param components The map of components with their matching names
         * @param content    The string patterns
         * @return The filtered components
         */
        public static Component[] getMatchingComponents(Map<String, Component> components, String... content) {
            return components.keySet().stream().filter(s -> Arrays.stream(content).anyMatch(s::contains)).map(components::get).toArray(Component[]::new);
        }

        /**
         * Texts all components from a Map with the matching texts in a language property
         *
         * @param components All components with their matching names
         * @param lang       The language properties containing component names and matching texts
         */
        public static void textComponents(Map<String, Component> components, ResourceBundle lang) {
            for (String name : components.keySet()) {
                Component component = components.get(name);
                if (lang.keySet().stream().anyMatch(prop -> prop.startsWith(name))) {
                    if (component.getClass() == JButton.class) {
                        ((JButton) component).setText(lang.getString(name));
                    } else if (component.getClass() == JLabel.class) {
                        ((JLabel) component).setText(lang.getString(name));
                    } else if (component.getClass() == JCheckBox.class) {
                        ((JCheckBox) component).setText(lang.getString(name));
                    } else if (component.getClass() == JTable.class) {
                        ((DefaultTableModel) ((JTable) component).getModel()).setColumnIdentifiers(lang.getString(name + ".header").split("#"));
                    }
                }
                if (lang.keySet().stream().anyMatch(prop -> prop.equals("tt." + name))) {
                    assert component instanceof JComponent;
                    ((JComponent) component).setToolTipText(lang.getString("tt." + name));
                }
            }
        }

        /**
         * Sets the foreground and the background color for each component in an array of components
         *
         * @param components Components that should be colored
         * @param foreground The foreground color
         * @param background The background color
         */
        public static void colorComponents(Component[] components, Color foreground, Color background) {
            for (Component component : components) {
                component.setForeground(foreground);
                component.setBackground(background);
            }
        }

        public static void doForAllComponents(JComponent container, Consumer<Component> consumer, Type... types) {
            List<Component> components = new ArrayList<>();
            addChildComponents(container, components);
            components
                    .stream()
                    .filter(c -> types.length == 0 || Arrays.asList(types).contains(c.getClass()))
                    .forEach(consumer);
        }

        private static void addChildComponents(JComponent container, List<Component> components) {
            for (Component component : container.getComponents()) {
                components.add(component);
                addChildComponents((JComponent) component, components);
            }
        }
    }
}
