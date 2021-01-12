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

/**
 * A class that allows you to encrypt and decrypt {@link String} objects via AES by choosing the hashing algorithm
 */
public class AES {

    private final Map<HashAlgorithm, SecretKeySpec> secretKeySpec;
    private final String pass;

    /**
     * Initializes a new object via saving a given password to en-/decrypt and generating {@link SecretKeySpec} objects for all {@link HashAlgorithm}s
     *
     * @param pass The password to be used for en/decrypting
     */
    public AES(String pass) {
        this.pass = pass;
        secretKeySpec = new HashMap<>();
        for (HashAlgorithm hashAlgorithm : HashAlgorithm.values()) {
            secretKeySpec.put(hashAlgorithm, getSecretKey(pass, hashAlgorithm));
        }
    }

    /**
     * Creates a {@link SecretKeySpec} object by getting a password as de/encryption key
     * and the {@link HashAlgorithm} to be used
     *
     * @param myKey         The password to be used for en/decrypting
     * @param hashAlgorithm The hashing algorithm
     * @return The generated SecretKeySpec
     */
    private static SecretKeySpec getSecretKey(String myKey, HashAlgorithm hashAlgorithm) {
        MessageDigest sha;
        byte[] key;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = hashAlgorithm.getDigestInstance();
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

    /**
     * @return True when {@link AES#pass} is empty
     */
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
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec.get(HashAlgorithm.SHA_256));
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
            return decrypt(strToDecrypt, HashAlgorithm.SHA_256);
        } catch (Exception e) {
            return decrypt(strToDecrypt, HashAlgorithm.SHA_1);
        }
    }

    /**
     * Decrypts a string with a given key via Advanced Encryption Standard (AES).
     *
     * @param strToDecrypt String to decrypt
     * @return Decrypted key
     * @throws WrongPasswordException Wrong password!
     */
    private String decrypt(String strToDecrypt, HashAlgorithm hashAlgorithm) throws WrongPasswordException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec.get(hashAlgorithm));
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new WrongPasswordException();
        }
    }

    /**
     * All available hashing algorithms
     */
    public enum HashAlgorithm {
        SHA_256, SHA_1;

        /**
         * Creates a {@link MessageDigest} instance with this enum's name
         *
         * @return The created {@link MessageDigest} instance
         * @throws NoSuchAlgorithmException There's no algorithm with this name
         */
        public MessageDigest getDigestInstance() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance(name().replace("_", "-"));
        }
    }

    /**
     * Is used in connection to {@link AES} objects.
     * The {@link AES} methods {@link AES#encrypt(String)} and {@link AES#decrypt(String)} throw this exception when the given password is incorrect
     */
    public static class WrongPasswordException extends RuntimeException {

    }
}
