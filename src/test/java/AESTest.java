import de.finnik.AES.AES;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AESTest {
    @Test
    public void decryptWithSamePass() {
        String string = randomString(8);
        AES aes = new AES(randomString(15));
        assertEquals(string, aes.decrypt(aes.encrypt(string)));
    }

    @Test(expected = AES.WrongPasswordException.class)
    public void decryptWithWrongPass() {
        String string = randomString(8);
        AES en = new AES(randomString(15)), de = new AES(randomString(14));
        assertEquals(string, de.decrypt(en.encrypt(string)));
    }

    public String randomString(int length) {
        char[] pass = new char[length];
        for (int i = 0; i < pass.length; i++) {
            pass[i] = (char)((int)(Math.random()*(60))+33);
        }
        return new String(pass);
    }
}
