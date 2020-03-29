package de.finnik.passvault;

import de.finnik.gui.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

import static de.finnik.gui.Var.*;

/**
 * Contains useful methods that concern this application
 */
public class PassUtils {

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
                InputStream is;
                for (Locale availableLocale : Locale.getAvailableLocales()) {
                    is = PassUtils.class.getResourceAsStream("/lang/" + availableLocale.getLanguage() + ".properties");
                    if (!languages.contains(availableLocale.getLanguage()) && is != null) {
                        languages.add(availableLocale.getLanguage());
                        is.close();

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

        /**
         * Loads the default properties for this application
         *
         * @return Default properties
         */
        private static Properties getDefaultProperties() {
            Properties properties = new Properties();
            String systemLang = Locale.getDefault().getLanguage();
            properties.setProperty("lang", FileUtils.availableLanguages().contains(systemLang) ? systemLang : "en");
            properties.setProperty("inactivity_time", "30");
            properties.setProperty("inactivity_lock", "true");
            return properties;
        }

        /**
         * Validates application properties and replaces invalid values with defaults from {@link FileUtils#getDefaultProperties()}
         *
         * @param properties The properties to validate
         */
        public static void validateProperties(Properties properties) {
            List<String> propertiesToDefault = new ArrayList<>();
            String property = "lang";
            if (!properties.containsKey(property) || !availableLanguages().contains(properties.getProperty(property)))
                propertiesToDefault.add(property);
            property = "inactivity_time";
            try {
                int inactivity_time = Integer.parseInt(properties.getProperty("inactivity_time"));
                if (!properties.containsKey(property) || !SettingsDialog.ALLOWED_INACTIVITY_TIME.matches(inactivity_time))
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                propertiesToDefault.add(property);
            }
            property = "inactivity_lock";
            if (!properties.containsKey(property) || (!properties.getProperty(property).equals("true") && !properties.getProperty(property).equals("false")))
                propertiesToDefault.add(property);
            propertiesToDefault.forEach(prop -> {
                LOG.info("Automatically adjusted property {} to default: {}", prop, getDefaultProperties().getProperty(prop));
                properties.setProperty(prop, getDefaultProperties().getProperty(prop));
            });
            if (!propertiesToDefault.isEmpty()) {
                storeProperties();
            }
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
        public static void textComponents(Map<String, Component> components, Properties lang) {
            for (String name : components.keySet()) {
                Component component = components.get(name);
                if (lang.keySet().stream().anyMatch(prop -> prop.toString().startsWith(name))) {
                    if (component.getClass() == JButton.class) {
                        ((JButton) component).setText(lang.getProperty(name));
                    } else if (component.getClass() == JLabel.class) {
                        ((JLabel) component).setText(lang.getProperty(name));
                    } else if (component.getClass() == JCheckBox.class) {
                        ((JCheckBox) component).setText(lang.getProperty(name));
                    } else if (component.getClass() == JTable.class) {
                        ((DefaultTableModel) ((JTable) component).getModel()).setColumnIdentifiers(lang.getProperty(name + ".header").split("#"));
                    }
                }
                if (lang.keySet().stream().anyMatch(prop -> prop.toString().startsWith("tt." + name))) {
                    assert component instanceof JComponent;
                    ((JComponent) component).setToolTipText(lang.getProperty("tt." + name));
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
    }


}
