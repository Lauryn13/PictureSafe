package com.example.picturesafe.enumerators;

public enum DataTypes {
    // müssen immer 4 Zeichen lang sein!
    NODATA("NODA"),
    TEXTDATA("0STR"),
    TXTDATA("0TXT");

    private final String text;

    DataTypes(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static DataTypes fromText(String text) {
        for (DataTypes dt : values()) {
            if (dt.getText().equals(text)) return dt;
        }
        return NODATA;
    }
}
