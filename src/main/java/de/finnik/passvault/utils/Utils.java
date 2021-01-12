package de.finnik.passvault.utils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Contains useful methods
 */
public class Utils {

    /**
     * Resize a given image to a new width and a new height
     *
     * @param input  The original image
     * @param width  The new width
     * @param height The new height
     * @return The resized image
     */
    public static BufferedImage resizeImage(BufferedImage input, int width, int height) {
        BufferedImage out = new BufferedImage(width, height, input.getType());
        final Graphics2D graphics2D = ((Graphics2D) out.getGraphics());
        graphics2D.drawImage(input, 0, 0, width, height, null);
        graphics2D.dispose();
        return out;
    }

    /**
     * Rotates a given image by a given angle in degrees
     *
     * @param img   The input image
     * @param angle The angle to rotate in degrees
     * @return The rotated image
     */
    public static BufferedImage rotateImage(BufferedImage img, double angle) {
        int w = img.getWidth();
        int h = img.getHeight();

        BufferedImage rotated = new BufferedImage(w, h, img.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.rotate(Math.toRadians(angle), w / 2.0, h / 2.0);
        graphic.drawImage(img, null, 0, 0);
        graphic.dispose();

        return rotated;
    }

    /**
     * Calculates the central X position of an element inside a container
     *
     * @param widthContainer Width of container
     * @param widthElement   Width of element
     * @return Calculated X position
     */
    public static int getCentralPosition(int widthContainer, int widthElement) {
        return (widthContainer - widthElement) / 2;
    }

    /**
     * Copies a string to the clipboard
     *
     * @param string string to copy
     */
    public static void copyToClipboard(String string) {
        StringSelection stringSelection = new StringSelection(string);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
    }

    /**
     * Opens URLs cross-platform
     */
    public static class Browser {
        /**
         * Allows you to open a given URL in the users default browser cross-platform
         *
         * @param url The URL to open
         * @throws URISyntaxException Wrong formatted URL
         * @throws IOException        Not able to open URL
         */
        public static void browse(String url) throws URISyntaxException, IOException {
            String os = System.getProperty("os.name").toLowerCase();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // Windows
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime runtime = Runtime.getRuntime();
                if (os.contains("mac")) {
                    //MacOS
                    runtime.exec("open " + url);
                } else {
                    // Linux
                    runtime.exec("xdg-open " + url);
                }
            }
        }
    }
}
