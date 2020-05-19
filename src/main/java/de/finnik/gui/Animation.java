package de.finnik.gui;

import de.finnik.passvault.Utils;

import java.awt.image.BufferedImage;

public class Animation {
    private final BufferedImage image;
    public int angle;

    public Animation(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage get() {
        return (angle == 0) ? image : Utils.rotateImage(image, angle);
    }

    public void stop() {
        angle = 0;
    }

    public boolean isFinished() {
        return angle % 360 == 0;
    }
}
