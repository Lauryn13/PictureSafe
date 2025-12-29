package com.example.picturesafe.enumerators;

import android.util.Log;

public enum DataTypes {
    // müssen immer 4 Zeichen lang sein!
    NODATA("NODA"),
    TEXTDATA("0STR"),
    TXTDATA("0TXT"),
    JPG("0JPG"),
    PNG("0PNG"),
//    MP3("0MP3"),
//    MP4("0MP4"),
//    EXCEL("XLSX"),
//    WORD("DOCX"),
    PDF("0PDF");

    private final String text;

    DataTypes(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static DataTypes fromText(String text) {
        // returns DataType from saved String in Picture
        for (DataTypes dt : values()) {
            if (dt.getText().equals(text)) return dt;
        }
        return NODATA;
    }

    public static DataTypes fromFile(String fileName){
        // returns DataType from imported File
        String lower = fileName.toLowerCase();
        Log.v("DataTypes", "SELECTING FILETYPE " + fileName);
        switch (lower.substring(lower.lastIndexOf("."))) {
            case ".txt":
                return DataTypes.TXTDATA;
            case ".jpg":
            case ".jpeg":
                return DataTypes.JPG;
            case ".png":
                return DataTypes.PNG;
            case ".pdf":
                return DataTypes.PDF;
            default:
                return DataTypes.NODATA;
        }

    }
}
