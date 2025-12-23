package com.example.picturesafe.enumerators;

public enum CompressionType {
    // müssen immer 2 Zeichen lang sein!
    // Reihenfolge: Byteumwandlung -> Komprimierung -> Verschlüsselung
    NOCOMPRESSION("NN"),
    AESNOCOMPRESSION("AN"),
    AESZSTDCOMPRESSION("ZA"),
    ZSTDCOMPRESSION("ZN");

    private final String text;

    CompressionType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static CompressionType fromText(String text) {
        for (CompressionType dt : values()) {
            if (dt.getText().equals(text)) return dt;
        }
        return NOCOMPRESSION;
    }
}
