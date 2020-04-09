import de.finnik.passvault.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class PasswordTest {

    @Test
    public void testWriteAndReadPasswords() throws IOException {
        List<Password> passwords = new ArrayList<>();
        passwords.add(new Password("pass","site","user","other"));
        passwords.add(new Password("pass2","site2","user2","other2"));
        passwords.add(new Password("pass3","site3","user3","other3"));

        File temp = File.createTempFile("passvault","bin");

        String pass = PasswordGenerator.generatePassword(new PasswordGenerator.PassChars[]{PasswordGenerator.PassChars.BIG_LETTERS}, (int) (Math.random() * 30) + 1);

        Password.savePasswords(passwords, temp,pass);
        assertEquals(passwords, Password.readPasswords(temp, pass));

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
