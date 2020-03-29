package de.finnik.passvault;

/**
 * Creates a password out of a given length and given characters
 */
public class PasswordGenerator {

    /**
     * Returns all characters in the ASCII code between two indices
     * @param first First index
     * @param second Second index
     * @return The characters between first and second assembled to a string
     */
    private static String GET_LETTERS_BETWEEN(int first, int second) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = first; i <= second; i++) {
            stringBuilder.append((char) i);
        }
        return stringBuilder.toString();
    }

    /**
     * Uses {@link PasswordGenerator#GET_LETTERS_BETWEEN(int, int)} to return a choice of characters assembled to a string
     *
     * @return Big letters (ABCDEFGHIJKLMNOPQRSTUVWXYZ)
     */
    public static String BIG_LETTERS() {
        return GET_LETTERS_BETWEEN(65, 90);
    }

    /**
     * Uses {@link PasswordGenerator#GET_LETTERS_BETWEEN(int, int)} to return a choice of characters assembled to a string
     *
     * @return Small letters (abcdefghijklmnopqrstuvwxyz)
     */
    public static String SMALL_LETTERS() {
        return GET_LETTERS_BETWEEN(97, 122);
    }

    /**
     * Uses {@link PasswordGenerator#GET_LETTERS_BETWEEN(int, int)} to return a choice of characters assembled to a string
     *
     * @return Numbers (0123456789) assembled to a string
     */
    public static String NUMBERS() {
        return GET_LETTERS_BETWEEN(48, 57);
    }

    /**
     * Uses {@link PasswordGenerator#GET_LETTERS_BETWEEN(int, int)} to return a choice of characters assembled to a string
     * @return Special Characters (!&#x22;#$%&#x26;&#x27;()*+,-./:;&#x3C;=&#x3E;?@[\]^_&#x60;{|}~)  assembled to a string
     */
    public static String SPECIAL_CHARACTERS() {
        return GET_LETTERS_BETWEEN(33, 47)+GET_LETTERS_BETWEEN(58, 64)+GET_LETTERS_BETWEEN(91, 96)+GET_LETTERS_BETWEEN(123, 126);
    }

    /**
     * Generates a password by assembling chars at random indexes of a given {@link String} until the password length equals to the given length.
     * @param chars The chars to use for the random password assembled to a string
     * @param length The length of the output password
     * @return The generated password
     */
    public static String generatePassword(String chars, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return stringBuilder.toString();
    }
}
