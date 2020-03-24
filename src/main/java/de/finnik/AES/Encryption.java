package de.finnik.AES;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.*;
import java.security.*;

public class Encryption {

    private byte[] key;

    public Encryption(String password) throws NoSuchAlgorithmException {
        key = createKey(password);
    }

    private byte[] createKey(String password) throws NoSuchAlgorithmException {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        key = md.digest(key);
        return key;
    }

    public String encrypt(String data) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ci.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        ci.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return data;
    }

    public String decrypt(String data) throws Exception{
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ci.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        ci.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return data;
    }
}
