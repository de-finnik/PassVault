package de.finnik.AES;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

public class AES {

    private final SecretKeySpec secretKeySpec;

    public AES(String pass) {
        secretKeySpec = getSecretKey(pass);
    }

    private static SecretKeySpec getSecretKey(String myKey) {
        MessageDigest sha;
        byte[] key;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while creating Secret key: "+e.getMessage());
        }
    }

    /**Encrypts a string via a given key with Advanced Encrpytion Standard (AES).
     * @param strToEncrypt String to encrypt
     * @return Encrypted key
     */
    public String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new WrongPasswordException();
        }
    }

    public static class WrongPasswordException extends RuntimeException {

    }
}
