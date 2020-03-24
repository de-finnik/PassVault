import de.finnik.passvault.Password;
import de.finnik.passvault.PasswordGenerator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import static de.finnik.passvault.PasswordGenerator.*;

public class PasswordGeneratorTest {
    @Test
    public void testBigLetters() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", BIG_LETTERS());
    }

    @Test
    public void testSmallLetters() {
        assertEquals("abcdefghijklmnopqrstuvwxyz", SMALL_LETTERS());
    }

    @Test
    public void testNumbers() {
        assertEquals("0123456789", NUMBERS());
    }

    @Test
    public void testSpecials() {
        assertEquals("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~", SPECIAL_CHARACTERS());
    }

    @Test
    public void testLengthOfGeneratedPassword() {
        int random;
        for (int z = 0; z < 5; z++) {
            random = (int)(Math.random()*100);
            assertEquals(random, generatePassword("Test",random).length());
        }
    }

    @Test
    public void testGenerateContainsMatchingChars() {
        List<String> allChars = Arrays.asList((BIG_LETTERS()+SMALL_LETTERS()+NUMBERS()+SPECIAL_CHARACTERS()).split(""));
        Collections.shuffle(allChars);
        String random = allChars.stream().limit(20).collect(Collectors.joining());

        String generatedPass = generatePassword(random, (int)(Math.random()*30+1));
        for(String ch:random.split("")) {
            generatedPass = generatedPass.replace(ch,"");
        }

        assertEquals(0,generatedPass.length());
    }
}
