package de.finnik.gui;

import de.finnik.gui.dialogs.PassDialog;
import de.finnik.passvault.InactivityListener;
import de.finnik.passvault.PassProperty;
import de.finnik.passvault.utils.PassUtils;
import de.finnik.passvault.utils.Utils;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

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
     * Properties that contain information about the application:
     * app.name
     * app.version
     * app.author
     */
    public static Properties APP_INFO;
    /**
     * Language properties that contain texts for all components
     */
    public static ResourceBundle LANG;
    /**
     * Images for the application components and dialogs
     */
    public static BufferedImage LOGO, ICON, ICON_SMALL, FRAME_ICON, COPY, EXTRACT, DRIVE_ICON, SETTINGS, REFRESH, REFRESH_DRIVE, CLOSE, WARNING, SELECTED, NOT_SELECTED, CHECK_MARK, QUESTION_MARK, HELP, FINNIK, HIDE, SHOW;
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
     * @return The language properties for property {@link PassProperty#LANG}
     */
    public static ResourceBundle loadLang() {
        return ResourceBundle.getBundle("passvault", new Locale(PassProperty.LANG.getValue()));
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
}
