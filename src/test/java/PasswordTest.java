import de.finnik.AES.AES;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.passwords.PasswordGenerator;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PasswordTest {

    @Test
    public void testWriteAndReadPasswords() throws IOException {
        List<Password> passwords = new ArrayList<>();
        passwords.add(new Password("pass", "site", "user", "other"));
        passwords.add(new Password("pass2", "site2", "user2", "other2"));
        passwords.add(new Password("pass3", "site3", "user3", "other3"));

        File temp = File.createTempFile("passvault", "bin");

        AES aes = new AES(new PasswordGenerator().generatePassword((int) (Math.random() * 30) + 1, PasswordGenerator.PassChars.BIG_LETTERS));

        Password.savePasswords(passwords, temp, aes);
        assertEquals(passwords, Password.readPasswords(temp, aes));

        temp.deleteOnExit();
    }

    @Test
    public void testLog() {
        Password password = new Password("pass", "site", "user", "other");

        assertFalse(!password.getPass().equals("") && Password.log(password, "").contains(password.getPass()));

        assertTrue(Password.log(password, "").contains(password.getSite()));
        assertTrue(Password.log(password, "").contains(password.getUser()));
        assertTrue(Password.log(password, "").contains(password.getOther()));
    }
}
