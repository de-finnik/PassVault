package de.finnik.passvault;

import de.finnik.AES.AES;
import de.finnik.passvault.utils.PassUtils;

import java.io.*;
import java.util.*;

import static de.finnik.gui.Var.LOG;

/**
 * The properties of the PassVault application
 */
public enum PassProperty {
    /**
     * PassVault's properties
     */
    LANG, INACTIVITY_LOCK, INACTIVITY_TIME, DRIVE_PASSWORD, SHOW_PASSWORDS_DOTTED, SHOW_MAIN_PASSWORD,
    GEN_LOW_LENGTH, GEN_UP_LENGTH, GEN_BIG, GEN_SMALL, GEN_NUM, GEN_SPE, REAL_RANDOM;

    /**
     * The file where the properties are saved
     */
    public static File PROPERTIES;

    /**
     * The value of the property
     */
    private String value;

    /**
     * Loads the properties from {@link PassProperty#PROPERTIES}
     */
    public static void load(AES aes) {
        Map<String, String> values = new HashMap<>();
        Arrays.stream(PassProperty.values()).forEach(prop -> values.put(prop.name(), prop.getDefault()));
        try (BufferedReader br = new BufferedReader(new FileReader(PROPERTIES))) {
            br.lines().filter(line -> !line.startsWith("#")).forEach(line -> {
                String[] split = line.split("#");
                if (split.length < 2) {
                    split = line.split("=");
                }
                String val;
                try {
                    try {
                        val = aes.decrypt(split[1]);
                    } catch (AES.WrongPasswordException | NullPointerException e) {
                        val = split[1];
                    }

                    if (values.containsKey(split[0])) {
                        // No encrypted key
                        values.put(split[0], val);
                    } else if (aes != null && values.containsKey(aes.decrypt(split[0]))) {
                        // Encrypted key
                        values.put(aes.decrypt(split[0]), val);
                    }
                } catch (AES.WrongPasswordException | ArrayIndexOutOfBoundsException ignore) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (PassProperty property : PassProperty.values()) {
            property.setValue(values.get(property.name()));
        }
        LOG.info("Loaded properties!");
    }

    /**
     * Stores the properties to {@link PassProperty#PROPERTIES}
     */
    public static void store(AES aes) {
        List<PassProperty> props = Arrays.asList(PassProperty.values());
        Collections.shuffle(props);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PROPERTIES))) {
            for (PassProperty property : props) {
                bw.write((property.encrypt() ? aes.encrypt(property.name()) : property.name()) + "#" + (property.encrypt() ? aes.encrypt(property.getValue()) : property.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            LOG.error("Error while saving properties!", e);
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
     * Sets the value for this property via {@link PassProperty#setValue(Object)} and takes the given AES to store the properties encrypted via {@link PassProperty#store(AES)}
     *
     * @param value The value to be assigned to this property
     * @param aes   The AES object that will be used for encrypting the stored properties
     * @return A boolean that tells whether the input was matching to the property's specifications via {@link PassProperty#matches(String)}
     */
    public boolean setValue(Object value, AES aes) {
        boolean matches = setValue(value);
        if (matches)
            LOG.info("Changed property {}", name());
        if (aes.passIsSet())
            store(aes);
        return matches;
    }

    /**
     * Sets the value of this property. Casts the given object to a string via {@link String#valueOf(Object)},
     * checks whether the value is valid ({@link PassProperty#matches(String)}) for this property and loads the default value ({@link PassProperty#getDefault()}) if not.
     *
     * @param value The value to be assigned to the property
     * @return Whether the input was valid or not
     */
    private boolean setValue(Object value) {
        String s = String.valueOf(value);
        if (matches(s)) {
            this.value = s;
        } else if (this.value == null) {
            this.value = getDefault();
        }
        return matches(s);
    }

    /**
     * Loads the default value of each property
     *
     * @return Default value of {@code this} property
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
        try {
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
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tells whether this property has to be encrypted when storing
     *
     * @return Needs to be encrypted when storing or not
     */
    private boolean encrypt() {
        return this != PassProperty.LANG
                && this != PassProperty.SHOW_MAIN_PASSWORD;
    }
}
