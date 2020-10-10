package de.finnik.AES;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AES {

    private final Map<HashMode, SecretKeySpec> secretKeySpec;
    private final String pass;

    public AES(String pass) {
        this.pass = pass;
        secretKeySpec = new HashMap<>();
        for (HashMode hashMode : HashMode.values()) {
            secretKeySpec.put(hashMode, getSecretKey(pass, hashMode));
        }
    }

    private static SecretKeySpec getSecretKey(String myKey, HashMode hashMode) {
        MessageDigest sha;
        byte[] key;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = hashMode.digest();
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while creating Secret key: " + e.getMessage());
        }
    }

    public String getPass() {
        return pass;
    }

    public boolean passIsSet() {
        return !pass.isEmpty();
    }

    /**
     * Encrypts a string via a given key with Advanced Encrpytion Standard (AES).
     *
     * @param strToEncrypt String to encrypt
     * @return Encrypted key
     */
    public String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec.get(HashMode.SHA_256));
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.toString());
        }
    }

    /**
     * Decrypts a string with a given key via Advanced Encryption Standard (AES).
     *
     * @param strToDecrypt String to decrypt
     * @return Decrypted key
     * @throws WrongPasswordException Wrong password!
     */
    public String decrypt(String strToDecrypt) throws WrongPasswordException {
        try {
            return decrypt(strToDecrypt, HashMode.SHA_256);
        } catch (Exception e) {
            return decrypt(strToDecrypt, HashMode.SHA_1);
        }
    }

    /**
     * Decrypts a string with a given key via Advanced Encryption Standard (AES).
     *
     * @param strToDecrypt String to decrypt
     * @return Decrypted key
     * @throws WrongPasswordException Wrong password!
     */
    private String decrypt(String strToDecrypt, HashMode hashMode) throws WrongPasswordException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec.get(hashMode));
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new WrongPasswordException();
        }
    }

    public enum HashMode {
        SHA_256, SHA_1;

        public MessageDigest digest() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance(name().replace("_", "-"));
        }
    }

    public static class WrongPasswordException extends RuntimeException {

    }
}
