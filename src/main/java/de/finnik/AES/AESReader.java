package de.finnik.AES;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Creates a reader that is able to read encrypted content
 */
public class AESReader extends BufferedReader {
    private final AES aes;

    /**
     * Initializes the reader with another {@link Reader} and an {@link AES} object to be used for decrypting
     *
     * @param in  The {@link Reader} to be used for calling the {@code super} constructor {@link BufferedReader#BufferedReader(Reader)}
     * @param aes The {@link AES} object to be used for decrypting
     */
    public AESReader(Reader in, AES aes) {
        super(in);
        this.aes = aes;
    }


    /**
     * Overrides the {@code super} method {@link BufferedReader#readLine()} by taking the output of the {@code super} method
     * and decrypting it with the help of {@link AESReader#aes} and then converting the unicode values back to a string via {@link AESReader#convertFromUnicode(String)}
     *
     * @return The decrypted read line
     * @throws IOException                Error while reading
     * @throws AES.WrongPasswordException Wrong password
     */
    @Override
    public String readLine() throws IOException, AES.WrongPasswordException {
        return convertFromUnicode(aes.decrypt(super.readLine()));
    }

    /**
     * Converts a string containing unicode values separated by ' ' into a string
     *
     * @param in The string containing unicode
     * @return The converted string
     */
    private String convertFromUnicode(String in) {
        String[] c = in.split(" ");
        StringBuilder out = new StringBuilder();
        for (String s : c) {
            out.append((char) Integer.parseInt(s, 16));
        }
        return out.toString();
    }
}
