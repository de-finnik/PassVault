package de.finnik.gui.customComponents;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * A border on the bottom of its component (e.g _______________)
 */
public class BottomBorder extends AbstractBorder {
    Color color;
    int thickness;

    /**
     * Creates the border and defines its color and thickness
     *
     * @param color     The color of the border
     * @param thickness The thickness of the border
     */
    public BottomBorder(Color color, int thickness) {
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        g.setColor(color);
        g.fillRect(0, height - thickness, width, thickness);
    }
}
