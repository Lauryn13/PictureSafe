package com.example.picturesafe.classes;

import java.security.SecureRandom;

public final class PictureUtils {
    /* ====== Helper functions for Picture class ====== */
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:',.<>?/";

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

    public static int[] readLSB(int pixel){
        int r = ((pixel >> 16) & 0xFF) % 2;
        int g = ((pixel >> 8)  & 0xFF) % 2;
        int b = (pixel         & 0xFF) % 2;

        return new int[] {r,g,b};
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

    public static byte[] binaryToBytes(int[] binData){
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

    public static String generate_Signature(int length){
        final SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
    public static String generate_Signature(){
        return generate_Signature(4);
    }

    public static Object[] convertMetaDataBytes(byte[] data, String name){
        Object[] result = new Object[11];

        result[0] = new String(data, 0, 5);
        result[1] = data[5] & 0xFF;
        result[2] = data[6] & 0xFF;
        result[3] = ((data[7] & 0xFF) << 8) | (data[8] & 0xFF);
        result[4] = ((data[9] & 0xFF) << 8) | (data[10] & 0xFF);
        result[5] = new String(data, 11, 4);
        result[6] = data[15] & 0xFF;
        result[7] = new String(data, 16, 4);
        result[8] = new String(data, 20, 2);
        result[9] = data[22] & 0xFF;
        result[10] = name;

        return result;
    }
}
