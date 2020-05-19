import de.finnik.passvault.PasswordGenerator;
import de.finnik.passvault.Utils;
import org.junit.Test;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

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

    @Test
    public void testClipboard() throws IOException, UnsupportedFlavorException {
        String string = PasswordGenerator.generatePassword(1, PasswordGenerator.PassChars.BIG_LETTERS);

        Utils.copyToClipboard(string);

        assertEquals(string, Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
    }
}
