import de.finnik.passvault.Utils;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class UtilsTest {
    @Test
    public void testResizeImage() {
        Dimension oldDim = new Dimension((int) (Math.random() * 500 + 1), (int) (Math.random() * 500 + 1));
        BufferedImage image = new BufferedImage(oldDim.width, oldDim.height, BufferedImage.TYPE_INT_RGB);

        Dimension newDim = new Dimension((int) (Math.random() * 500 + 1), (int) (Math.random() * 500 + 1));
        BufferedImage resized = Utils.resizeImage(image, newDim.width, newDim.height);

        assertEquals(newDim.width, resized.getWidth());
        assertEquals(newDim.height, resized.getHeight());
    }
}
