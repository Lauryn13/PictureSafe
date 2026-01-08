package com.example.picturesafe.classes;

import android.util.Log;

import java.security.SecureRandom;
import java.util.Arrays;

public final class PictureUtils {
    /* ====== Helper functions for Picture class ====== */
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:',.<>?/";
    private static final String TAG = "PictureUtils";

    private PictureUtils() {}

    public static int setLSB(int pixel, int bitR, int bitG, int bitB) {
        int a = (pixel >> 24) & 0xFF;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8)  & 0xFF;
        int b =  pixel        & 0xFF;

        r = (r & ~1) | (bitR & 1);
        g = (g & ~1) | (bitG & 1);
        b = (b & ~1) | (bitB & 1);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static byte[] readLSB(int pixel){
        byte r = (byte) (((pixel >> 16) & 0xFF) & 1);
        byte g = (byte) (((pixel >> 8)  & 0xFF) & 1);
        byte b = (byte) (( pixel        & 0xFF) & 1);

        return new byte[] {r,g,b};
    }

    public static int[] bytesToBinary(byte[] bytes) {
        int[] bits = new int[bytes.length * 8];
        int index = 0;

        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                bits[index++] = (b >> i) & 1;
            }
        }
        return bits;
    }

    public static byte[] binaryToBytes(byte[] binData){
        int byteLength = (binData.length + 7) / 8;
        byte[] result = new byte[byteLength];

        for (int i = 0; i < binData.length; i++) {
            int byteIndex = i / 8;
            result[byteIndex] <<= 1;
            result[byteIndex] |= (binData[i] & 1);
        }

        // Letztes Byte ggf. auffüllen, falls nicht 8 Bits
        int remainder = binData.length % 8;
        if (remainder != 0) {
            result[byteLength - 1] <<= (8 - remainder);
        }

        return result;
    }

    public static String generate_signature(int length){
        final SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
    public static String generate_signature(){
        return generate_signature(4);
    }

    public static String generate_info_text(Picture[] pictures, int curIdx){
        Picture curPicture = pictures[curIdx];
        float storable_mb = 0;
        float picture_storable_mb = ((float) Math.round((float) curPicture.storeable_data_in_byte / 1000 / 10)) / 100; // durch 100 um auf 2 dezimalstellen zu runden
        for(Picture pic : pictures) {
            storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;
        }

        return "Aktuelles Foto:\nAuflösung: " + curPicture.width + "x" + curPicture.height + "\nSpeicherbare Datenmenge: " + picture_storable_mb + " Mb"  + "\n\nIn allen Bildern zusammen könnten " + storable_mb + " Mb Daten gespeichert werden.";
    }

    public static Object[] convertMetaDataBytes(byte[] data, String name){
        Object[] result = new Object[12];

        result[0] = new String(data, 0, 5);
        result[1] = data[5] & 0xFF;
        result[2] = data[6] & 0xFF;
        result[3] = ((data[7] & 0xFF) << 8) | (data[8] & 0xFF);
        result[4] = ((data[9] & 0xFF) << 8) | (data[10] & 0xFF);
        result[5] = new String(data, 11, 4);
        result[6] = data[15] & 0xFF;
        result[7] = new String(data, 16, 4);
        result[8] = new String(data, 20, 2);
        result[9] = ((data[22] & 0xFF) << 24 ) | ((data[23] & 0xFF) << 16) | ((data[24] & 0xFF) << 8) | (data[25] & 0xFF);
        result[10] = data[26] & 0xFF;
        result[11] = name;

        return result;
    }

    public static int[] int_to_16bit_array(int value){
        int[] bits = new int[16];

        for (int i = 15; i >= 0; i--) {
            bits[15 - i] = ((value >> i) & 1);
        }
        return bits;
    }

    public static byte[] remove_check_signature(byte[] bits, int imageWidth, int[] signatureBits) {
        int bitsPerLine = imageWidth * 3;
        byte[] out = new byte[bits.length];
        int r = 0; // = bits gelesen (inkludiert die Signatur)
        int w = 0; // = bits geschrieben (exkludiert die Signatur)

        while (r + 16 <= bits.length) {
            // Signaturstart
            int lineBitsLeft = bitsPerLine;
            int sigStart = 0;
            for (int i = 0; i < 16; i++) {
                sigStart = (sigStart << 1) | (bits[r++] & 1);
            }
            lineBitsLeft -= 16;

            // Daten vor Signatur
            int pre = Math.min(sigStart, bits.length - r);
            System.arraycopy(bits, r, out, w, pre);
            r += pre;
            w += pre;
            lineBitsLeft -= pre;

            // Signatur prüfen
            for (int i = 0; i < 32; i++) {
                if ((bits[r++] & 1) != signatureBits[i]){
                    Log.v(TAG, "Signature Error");
                    return null;
                }
            }
            lineBitsLeft -= 32;

            // Rest der Zeile
            int rest = Math.min(lineBitsLeft, bits.length - r); // Berechnet Rest der Zeile (entweder bis Bildende oder bis Datenende)
            System.arraycopy(bits, r, out, w, rest);
            r += rest;
            w += rest;
        }

        return java.util.Arrays.copyOf(out, w);
    }
}
