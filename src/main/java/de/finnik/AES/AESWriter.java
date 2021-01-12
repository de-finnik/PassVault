package de.finnik.AES;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Creates a writer that is able to write encrypted content
 */
public class AESWriter extends BufferedWriter {
    private final AES aes;

    /**
     * Initializes the writer with another {@link Writer} and an {@link AES} object to be used for encrypting
     *
     * @param out The {@link Writer} to be used for calling the {@code super} constructor {@link BufferedWriter#BufferedWriter(Writer)}
     * @param aes The {@link AES} object to be used for encrypting
     */
    public AESWriter(Writer out, AES aes) {
        super(out);
        this.aes = aes;
    }

    /**
     * Overrides the {@code super} method {@link BufferedWriter#write(String)} ()} by converting the input to unicode values via {@link AESWriter#convertToUnicode(String)}
     * then decrypting these unicode values with the help of {@link AESWriter#aes} object and then writing this encrypted output via the {@code super} method
     *
     * @param str The {@link String} to be written encrypted
     * @throws IOException                Error while writing
     * @throws AES.WrongPasswordException Wrong password
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public void write(String str) throws IOException {
        super.write(aes.encrypt(convertToUnicode(str)));
    }

    /**
     * Converts a string to a string containing the unicode values for each character separated by ' '
     *
     * @param in The string to convert
     * @return The converted string containing unicode values separated by ' '
     */
    private String convertToUnicode(String in) {
        char[] c = in.toCharArray();
        StringBuilder out = new StringBuilder();
        for (char value : c) {
            out.append(Integer.toHexString(value | 0x10000).substring(1));
            out.append(" ");
        }
        return out.toString();
    }
}
