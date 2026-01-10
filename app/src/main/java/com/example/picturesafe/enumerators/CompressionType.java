package com.example.picturesafe.enumerators;

import android.util.Log;

import com.example.picturesafe.classes.Compression;
import com.example.picturesafe.components.PictureSafeCheckBox;
import com.example.picturesafe.components.PictureSafeDropDown;

import java.io.IOException;
import java.util.zip.Deflater;

public enum CompressionType {
    // müssen immer 2 Zeichen lang sein!
    // Reihenfolge: Byteumwandlung -> Komprimierung -> Verschlüsselung

    // Keine Komprimierung
    NO("NN"),
    NOAES("NA"),

    // Schnelle Komprimierung mit LZ4
    LZ4("LN"),
    LZ4AES("LA"),

    // Ausgeglichene Komprimierung Deflate
    DEFLATE("DN"),
    DEFLATEAES("DA"),

    // Maximale Komprimierung mit Brotli oder Deflate lvl 9
    MAXDEFLATE("MN"),
    MAXDEFLATEAES("MA");

    public final String text;

    CompressionType(String text) {
        this.text = text;
    }

    public static CompressionType fromText(String text) {
        for (CompressionType dt : values()) {
            if (dt.text.equals(text)) return dt;
        }
        return NO;
    }

    public boolean uses_encryption() {
        switch (this) {
            case NOAES:
            case LZ4AES:
            case DEFLATEAES:
            case MAXDEFLATEAES:
                return true;
            default:
                return false;
        }
    }

    public static CompressionType fromUI(PictureSafeCheckBox checkBox, PictureSafeDropDown dropDown){
        CompressionType compType;
        CompressionDropdown comDropDown = dropDown.getSelectedItem();
        Log.v("COMPRESSIONTYPE", "comDropDown: " + comDropDown);

        switch(comDropDown){
            case FAST:
                compType = CompressionType.LZ4AES;
                break;
            case DEFAULT:
                compType = CompressionType.DEFLATEAES;
                break;
            case MAX:
                compType = CompressionType.MAXDEFLATEAES;
                break;
            default:
                compType = CompressionType.NOAES;
        }
        Log.v("COMPRESSIONTYPE", "compType: " + compType.text + " checkBox: " + checkBox.isChecked());
        if(!checkBox.isChecked()){
            return compType.remove_aes();
        }
        else
            return compType;
    }

    public CompressionType remove_compression(){
        switch (this) {
            case NO:
            case LZ4:
            case DEFLATE:
            case MAXDEFLATE:
                return CompressionType.NO;
            case NOAES:
            case LZ4AES:
            case DEFLATEAES:
            case MAXDEFLATEAES:
                return CompressionType.NOAES;
            default:
                return this;
        }
    }

    public CompressionType remove_aes() {
        switch (this) {
            case LZ4AES:
            case LZ4:
                return LZ4;
            case DEFLATEAES:
            case DEFLATE:
                return DEFLATE;
            case MAXDEFLATEAES:
            case MAXDEFLATE:
                return MAXDEFLATE;
            case NOAES:
            default:
                return NO;
        }
    }

    public byte[] compress_data(byte[] data) throws IOException {
        // Komprimiert die Daten je nach ausgewählten Algorithmus.
        // Liefert null, wenn die daten unkomprimiert kürzer sind.
        byte[] compressed_data = null;

        switch (this) {
            case NO:
            case NOAES:
                return null;
            case LZ4:
            case LZ4AES:
                compressed_data = Compression.compressLZ4(data);
                break;
            case DEFLATE:
            case DEFLATEAES:
                compressed_data = Compression.compressDeflate(data, Deflater.DEFAULT_COMPRESSION);
                break;
            case MAXDEFLATE:
            case MAXDEFLATEAES:
                compressed_data = Compression.compressDeflate(data, Deflater.BEST_COMPRESSION);
                break;
            default:
                break;
        }

        return compressed_data;
    }

    public byte[] decompress_data(byte[] data, int originalSize){
        byte[] decompressed_data = null;

        switch (this) {
            case NO:
            case NOAES:
                decompressed_data = data;
                break;
            case LZ4:
            case LZ4AES:
                decompressed_data = Compression.decompressLZ4(data, originalSize);
                break;
            case DEFLATE:
            case DEFLATEAES:
            case MAXDEFLATE:
            case MAXDEFLATEAES:
                decompressed_data = Compression.decompressDeflate(data);
                break;
            default:
                break;
        }
        return decompressed_data;
    }
}
