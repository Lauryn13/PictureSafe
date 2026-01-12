package com.example.picturesafe.classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AESEncryption
 *
 * Die Klasse kümmert sich innerhalb des Projektes um die Verschlüsselung und Entschlüsselung von Byte-Arrays.
 */
public class AESEncryption {

    /** generateKey
     * Generiert den SecretKey, welcher zum Ver-/Entschlüsseln der Daten benötigt wird.
     *
     * @param password genutztes Passwort als Char-Array
     * @param salt Zufällige Salt-Daten, entweder frisch Generiert oder aus den Bilddaten gelesen.
     * @return SecretKey zum Ver-/Entschlüsseln
     * @throws GeneralSecurityException Bei Fehlern beim Generieren des Schlüssels, beipielsweise durch falsche Passwort eingaben.
     */
    private static SecretKey generateKey(char[] password, byte[] salt) throws GeneralSecurityException{
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        KeySpec spec = new PBEKeySpec(password, salt, 77777, 256);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    /** encrypt
     * Verschlüsselt ein übergebenes Byte-Array
     *
     * @param data Byte-Array, welches verschlüsselt werden soll
     * @param password Passwort zum generieren des SecretKeys
     * @return Verschlüsseltes Byte-Array
     * @throws GeneralSecurityException Bei Fehlern beim Generieren des Schlüssels, beipielsweise durch falsche Passwort eingaben.
     * @throws IOException Fehlern in Schreib-/Lesevorgängen
     */
    public static byte[] encrypt(byte[] data, char[] password) throws GeneralSecurityException, IOException{
        byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);

        // Erstellung des SecretKeys und des genutzten Algorithmus
        SecretKey key = generateKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(data);

        // Zusammenfügen von Salt, Initialisierungsvektor und der verschlüsselten Daten
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(salt);
        out.write(iv);
        out.write(encrypted);

        return out.toByteArray();
    }

    /**
     * decrypt
     * Entschlüsselt ein übergebenes Byte-Array
     *
     * @param encryptedData Verschlüsselte Daten als Byte-Array
     * @param password Passwort zum Entschlüsseln als Char-Array
     * @return Entschlüsselte Byte-Daten ohne Salt und Initialisierungsvektor
     * @throws GeneralSecurityException Bei Fehlern beim Generieren des Schlüssels, beipielsweise durch falsche Passwort eingaben.
     */
    public static byte[] decrypt(byte[] encryptedData, char[] password) throws GeneralSecurityException {
        byte[] salt = Arrays.copyOfRange(encryptedData, 0, 16);
        byte[] iv   = Arrays.copyOfRange(encryptedData, 16, 28);
        byte[] data = Arrays.copyOfRange(encryptedData, 28, encryptedData.length);

        SecretKey key = generateKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        return cipher.doFinal(data);
    }
}
