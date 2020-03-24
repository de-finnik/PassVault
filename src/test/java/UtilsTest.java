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
    public void testClipboard() throws IOException, UnsupportedFlavorException {
        String string = PasswordGenerator.generatePassword(PasswordGenerator.BIG_LETTERS(), 15);

        Utils.copyToClipboard(string);

        assertEquals(string, (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
    }
}
