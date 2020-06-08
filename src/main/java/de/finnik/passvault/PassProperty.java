package de.finnik.passvault;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import static de.finnik.gui.Var.LOG;
import static de.finnik.gui.Var.PASSWORDS;

/**
 * The properties of the application
 */
public enum PassProperty {
    LANG, INACTIVITY_LOCK, INACTIVITY_TIME, DRIVE_PASSWORD, SHOW_PASSWORDS_DOTTED, SHOW_MAIN_PASSWORD, GEN_LOW_LENGTH, GEN_UP_LENGTH, GEN_BIG, GEN_SMALL, GEN_NUM, GEN_SPE, REAL_RANDOM;

    /**
     * The file where the properties are saved
     */
    public static final File PROPERTIES = new File(PASSWORDS.getParent(), "config.properties");

    /**
     * The value of the property
     */
    private String value;

    /**
     * Loads the properties from {@link PassProperty#PROPERTIES}
     */
    public static void load() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(PROPERTIES));
        } catch (IOException e) {
            LOG.error("Error while loading config.properties!", e);
        }

        for (PassProperty property : PassProperty.values()) {
            property.setValue(properties.getProperty(property.name(), property.getDefault()));
        }
    }

    /**
     * Stores the properties to {@link PassProperty#PROPERTIES}
     */
    public static void store() {
        Properties properties = new Properties();
        Arrays.stream(PassProperty.values()).forEach(prop -> properties.setProperty(prop.name(), String.valueOf(prop.getValue())));
        try {
            properties.store(new FileWriter(PROPERTIES), "PassVault Settings");
        } catch (IOException e) {
            LOG.error("Error while saving config.properties!", e);
        }
    }

    /**
     * Returns the value of this property
     *
     * @return Value of this property
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this property. Casts the given object to a string via {@link String#valueOf(Object)},
     * checks whether the value is valid ({@link PassProperty#matches(String)}) for this property and loads the default value ({@link PassProperty#getDefault()}) if not.
     *
     * @param value The value to be assigned to the property
     * @return Whether the input was valid or not
     */
    public boolean setValue(Object value) {
        String s = String.valueOf(value);
        if (matches(s)) {
            if (this.value == null) {
                LOG.info("Loaded property {}: {}!", this.name(), s);
            } else {
                LOG.info("Set property {} to {}!", this.name(), s);
            }
            this.value = s;
        } else if (this.value == null) {
            this.value = getDefault();
        }
        store();
        return matches(s);
    }

    /**
     * Loads the default value of each property
     *
     * @return Default value of {@code this}
     */
    private String getDefault() {
        switch (this) {
            case LANG:
                String systemLang = Locale.getDefault().getLanguage();
                return PassUtils.FileUtils.availableLanguages().contains(systemLang) ? systemLang : "en";
            case INACTIVITY_LOCK:
            case SHOW_PASSWORDS_DOTTED:
            case SHOW_MAIN_PASSWORD:
            case GEN_BIG:
            case GEN_SMALL:
            case GEN_NUM:
            case GEN_SPE:
                return "true";
            case REAL_RANDOM:
                return "false";
            case GEN_LOW_LENGTH:
                return "12";
            case GEN_UP_LENGTH:
                return "18";
            case INACTIVITY_TIME:
                return "30";
            case DRIVE_PASSWORD:
                return "";
        }
        return null;
    }

    /**
     * Checks whether a input is valid for {@code this} property.
     *
     * @param value The input to check
     * @return Valid or not
     */
    private boolean matches(String value) {
        switch (this) {
            case LANG:
                return PassUtils.FileUtils.availableLanguages().contains(value);
            case INACTIVITY_LOCK:
            case SHOW_PASSWORDS_DOTTED:
            case SHOW_MAIN_PASSWORD:
            case GEN_BIG:
            case GEN_SMALL:
            case GEN_NUM:
            case GEN_SPE:
            case REAL_RANDOM:
                return value.equals("true") || value.equals("false");
            case GEN_LOW_LENGTH:
            case GEN_UP_LENGTH:
                int z = Integer.parseInt(value);
                return z >= 5 && z <= 30;
            case INACTIVITY_TIME:
                try {
                    int i = Integer.parseInt(value);
                    return i >= 10 && i <= 3600;
                } catch (NumberFormatException ignored) {
                }
            case DRIVE_PASSWORD:
                return value != null;
            default:
                return false;
        }
    }
}
