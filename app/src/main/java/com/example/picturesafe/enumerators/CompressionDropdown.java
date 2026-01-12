package com.example.picturesafe.enumerators;

/** CompressionDropdown
 *  Enum um die Optionen des Komprimierens anzuzeigen (Frontend)
 */
public enum CompressionDropdown {
    NONE("Kein Komprimieren"),
    FAST("schnelles Komprimieren"),
    DEFAULT("ausgewogenes Komprimieren"),
    MAX("Maximales Komprimieren");
    public final String text;

    CompressionDropdown(String text) {
        this.text = text;
    }

    /** fromText
     *  Konvertiert einen String in einen CompressionDropdown-Wert
     *
     * @param text String der umgewandelt werden soll
     * @return CompressionDropdown-Wert
     */
    public static CompressionDropdown fromText(String text){
        for (CompressionDropdown dt : values())
            if (dt.text.equals(text))
                return dt;

        return NONE;
    }

    /** getAllStrings
     *  Gibt alle Optionen des CompressionDropdowns als String-Array zurück
     *
     * @return Optionen als String-Array
     */
    public static String[] getAllStrings(){
        String[] value_strings = new String[values().length];

        for(int i = 0; i < values().length; i++)
            value_strings[i] = values()[i].text;

        return value_strings;
    }
}
