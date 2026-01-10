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
//    ZIP("ZIP"),
//    PPTX("PPTX"),
//    CSV("CSV"),

    PDF("0PDF");

    public final String text;

    DataTypes(String text) {
        this.text = text;
    }

    public static DataTypes fromText(String text) {
        // returns DataType from saved String in Picture
        for (DataTypes dt : values()) {
            if (dt.text.equals(text)) return dt;
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

    public static String getExtension(DataTypes dataType){
        if(dataType == DataTypes.NODATA){
            return ".bin";
        }
        if(dataType.text.startsWith("0")){
            return "." + dataType.text.substring(1).toLowerCase();
        } else {
            return "." + dataType.text.toLowerCase();
        }
    }
}
