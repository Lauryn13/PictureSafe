package com.example.picturesafe.classes;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {
    private static SecretKey generateKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        KeySpec spec = new PBEKeySpec(password, salt, 77777, 256);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encrypt(byte[] data, char[] password) throws Exception {
        byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);

        Log.v("ENCRYPTION", "password: " + Arrays.toString(password));
        Log.v("ENCRYPTION", "salt: " + Arrays.toString(salt));
        SecretKey key = generateKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(data);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(salt);
        out.write(iv);
        out.write(encrypted);

        return out.toByteArray();
    }

    public static byte[] decrypt(byte[] encryptedData, char[] password)
            throws Exception {

        byte[] salt = Arrays.copyOfRange(encryptedData, 0, 16);
        byte[] iv   = Arrays.copyOfRange(encryptedData, 16, 28);
        byte[] data = Arrays.copyOfRange(encryptedData, 28, encryptedData.length);

        Log.v("ENCRYPTION", "password: " + Arrays.toString(password));
        Log.v("ENCRYPTION", "salt: " + Arrays.toString(salt));

        SecretKey key = generateKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        return cipher.doFinal(data);
    }
}
