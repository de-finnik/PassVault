import de.finnik.passvault.passwords.PasswordGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.finnik.passvault.passwords.PasswordGenerator.PassChars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PasswordGeneratorTest {
    @Test
    public void testBigLetters() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", PassChars.BIG_LETTERS.get());
    }

    @Test
    public void testSmallLetters() {
        assertEquals("abcdefghijklmnopqrstuvwxyz", PassChars.SMALL_LETTERS.get());
    }

    @Test
    public void testNumbers() {
        assertEquals("0123456789", PassChars.NUMBERS.get());
    }

    @Test
    public void testSpecials() {
        assertEquals("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~", PassChars.SPECIAL_CHARACTERS.get());
    }

    @Test
    public void testLengthOfGeneratedPassword() {
        int random1, random2;
        PasswordGenerator generator = new PasswordGenerator();
        for (int z = 0; z < 5; z++) {
            random1 = randomLength();
            random2 = randomLength();
            String generated = generator.generatePassword(random1, random2, PassChars.BIG_LETTERS);
            Assert.assertTrue(generated.length() >= Math.min(random1, random2) && generated.length() <= Math.max(random1, random2));
        }
    }

    @Test
    public void testGenerateContainsMatchingChars() {
        List<PassChars> all = Arrays.asList(PassChars.values());
        PasswordGenerator generator = new PasswordGenerator();
        for (int i = 0; i <= 500; i++) {
            Collections.shuffle(all);
            String password;
            password = generator.generatePassword(randomLength(), randomLength(), all.get(0));
            if (Arrays.stream(all.get(0).get().split("")).noneMatch(password::contains)) {
                fail();
            }
        }
    }

    private int randomLength() {
        return (int) (Math.random() * 25) + 5;
    }
}
