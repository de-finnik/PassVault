package de.finnik.gui.customComponents;

import de.finnik.passvault.utils.Utils;

import java.awt.image.BufferedImage;

/**
 * A class that allows you to rotate an image
 */
public class Animation {
    /**
     * The image to rotate
     */
    private final BufferedImage image;
    /**
     * The angle that the image is currently rotated
     */
    public int angle;

    /**
     * Initializes the image
     *
     * @param image The image to be rotated
     */
    public Animation(BufferedImage image) {
        this.image = image;
    }

    /**
     * @return The image to be drawn currently, if {@link Animation#angle} != 0, image will be rotated via {@link Utils#rotateImage(BufferedImage, double)}
     */
    public BufferedImage get() {
        return (angle == 0) ? image : Utils.rotateImage(image, angle);
    }

    /**
     * Stops the animation
     */
    public void stop() {
        angle = 0;
    }

    /**
     * @return True when {@link Animation#angle} % 360 == 0
     */
    public boolean isFinished() {
        return angle % 360 == 0;
    }
}
