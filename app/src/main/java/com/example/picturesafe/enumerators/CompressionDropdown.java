package com.example.picturesafe.enumerators;

import android.util.Log;

public enum CompressionDropdown {
    NONE("Kein Komprimieren"),
    FAST("schnelles Komprimieren"),
    DEFAULT("ausgewogenes Komprimieren"),
    MAX("Maximales Komprimieren");
    public final String text;

    CompressionDropdown(String text) {
        this.text = text;
    }

    public static CompressionDropdown from_text(String text){
        Log.v("DROPDOWN", text);
        for (CompressionDropdown dt : values()) {
            if (dt.text.equals(text)) return dt;
        }
        return NONE;
    }

    public static String[] get_all_strings(){
        String[] value_strings = new String[values().length];
        for(int i = 0; i < values().length; i++){
            value_strings[i] = values()[i].text;
        }
        return value_strings;
    }
}
