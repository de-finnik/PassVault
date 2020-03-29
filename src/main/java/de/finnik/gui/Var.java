package de.finnik.gui;

import de.finnik.passvault.*;
import org.slf4j.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * All static variables that concern multiple classes
 */
public class Var {

    final public static Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    /**
     * The current main frame
     */
    public static Window FRAME;
    /**
     * The PassDialog with matching settings for this application
     */
    public static PassDialog DIALOG;
    /**
     * The file where passwords are saved
     */
    public static File PASSWORDS;
    /**
     * The file where properties are saved
     */
    public static File PROPERTIES;
    /**
     * Properties that contain information about the application:
     * app.name
     * app.version
     * app.author
     */
    public static Properties APP_INFO;
    /**
     * The application properties:
     * lang
     */
    public static Properties PROPS;
    /**
     * Language properties that contain texts for all components
     */
    public static Properties LANG;
    /**
     * Images for the application components and dialogs
     */
    public static BufferedImage LOGO, ICON, ICON_SMALL, FRAME_ICON, COPY, EXTRACT, SETTINGS, CLOSE, WARNING, SELECTED, NOT_SELECTED, CHECK_MARK, QUESTION_MARK, HELP, FINNIK;
    public static Color FOREGROUND = Color.white;
    public static Color BACKGROUND = Color.black;
    /**
     * The logger for every class
     */
    public static Logger LOG;
    /**
     * The raleway font (© Matt McInerney, Pablo Impallari, Rodrigo Fuenzalida; licensed under the SIL Open Font License, Version 1.1.)
     */
    public static Font RALEWAY;
    /**
     * All components with their matching names to get their texts and tool-tip-texts from {@link Var#LANG} properties
     */
    public static Map<String, Component> COMPONENTS;

    public static InactivityListener INACTIVITY_LISTENER;

    /**
     * Overrides {@link PassUtils.FileUtils#loadLang} method with matching variables from this class
     *
     * @return The language properties for property "lang" from {@link Var#PROPS}
     */
    public static Properties loadLang() {
        return PassUtils.FileUtils.loadLang(PROPS.getProperty("lang"));
    }

    /**
     * Overrides {@link PassUtils.GUIUtils#getMatchingComponents(Map, String...)} with matching variables from this class
     *
     * @param content The string patterns by which the components are filtered
     * @return The list of components matching to the string patterns
     */
    public static Component[] getMatchingComponents(String... content) {
        return PassUtils.GUIUtils.getMatchingComponents(COMPONENTS, content);
    }

    /**
     * Overrides {@link PassUtils.GUIUtils#textComponents} method with matching variables from this class
     * Texts the {@link Var#COMPONENTS} components with the matching texts from {@link Var#LANG}
     */
    public static void textComponents() {
        PassUtils.GUIUtils.textComponents(COMPONENTS, LANG);
    }

    /**
     * Overrides {@link Utils#sizeFont(Font, float)} method with matching variables from this class
     *
     * @param size The new font size
     * @return The resized font
     */
    public static Font raleway(int size) {
        return Utils.sizeFont(RALEWAY, size);
    }

    /**
     * Stores the application properties {@link Var#PROPS} to {@link Var#PROPERTIES}
     */
    public static void storeProperties() {
        try {
            PROPS.store(new FileWriter(PROPERTIES), "PassVault settings");
        } catch (IOException e) {
            LOG.error("Error while saving properties!");
        }
    }
}
