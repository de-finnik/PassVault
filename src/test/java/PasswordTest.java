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

        AES aes = new AES(new PasswordGenerator().generatePassword((int) (Math.random() * 25) + 5, (int) (Math.random() * 25) + 5, PasswordGenerator.PassChars.BIG_LETTERS));

        Password.savePasswords(passwords, temp, aes);
        assertEquals(passwords, Password.readPasswords(temp, aes));

        temp.deleteOnExit();
    }

    @Test
    public void testLog() {
        Password password = new Password("pass", "site", "user", "other");

        String log = Password.log(password, "");
        assertFalse(log.contains(password.getPass()));
        assertTrue(log.contains(password.getSite()));
        assertTrue(log.contains(password.getUser()));
        assertTrue(log.contains(password.getOther()));
    }

    @Test
    public void testUpdateModified() throws InterruptedException {
        Password password = new Password("pass", "site", "user", "other");
        long lastModified = password.lastModified();
        Thread.sleep(10);
        password.setPass("passNew");
        assertTrue(lastModified < password.lastModified());
    }

    @Test
    public void testEquals() {
        Password password = new Password("pass", "site", "user", "other");
        Password password2 = new Password(password);
        assertEquals(password, password2);
    }

    @Test
    public void testEqualsInformation() {
        Password password = new Password("pass", "site", "user", "other");
        Password password2 = new Password("pass", "site", "user", "other");
        assertTrue(Password.equalsInformation(password, password2));
    }

    @Test
    public void testIsEmpty() {
        Password password = new Password("pass", "site", "user", "other");
        assertFalse(password.isEmpty());
        password.setPass("");
        password.setSite("");
        password.setUser("");
        password.setOther("");
        assertTrue(password.isEmpty());
    }
}
