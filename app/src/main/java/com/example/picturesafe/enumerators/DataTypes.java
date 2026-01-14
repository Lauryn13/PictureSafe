package com.example.picturesafe.enumerators;

/** DataTypes
 *  Datentypen, die in einem Bild gespeichert werden.
 *  Dient hauptsächlich zur Umwandlung wieder zurück zur Datei
 *
 *  Müssen immer 4 Zeichen sein -> zur richtigen Speicherung in den Metadaten
 */
public enum DataTypes {
    NODATA("NODA"),
    TEXTDATA("0STR"),
    TXTDATA("0TXT"),
    JPG("0JPG"),
    PNG("0PNG"),
    MP3("0MP3"),
    MP4("0MP4"),
    EXCEL("XLSX"),
    WORD("DOCX"),
    ZIP("ZIP"),
    PPTX("PPTX"),
    CSV("CSV"),
    PDF("0PDF");

    public final String text;

    DataTypes(String text) {
        this.text = text;
    }

    /** fromText
     *  Konvertiert einen String aus den Metadaten in einen DataTypes-Wert
     *
     * @param text
     * @return DataTypes-Wert
     */
    public static DataTypes fromText(String text) {
        // returns DataType from saved String in Picture
        for (DataTypes dt : values())
            if (dt.text.equals(text))
                return dt;

        return NODATA;
    }

    /** fromFile
     *  Konvertiert einen String aus dem Dateinamen in einen DataTypes-Wert
     *
     * @param fileName Dateiname der zu speichernden Datei
     * @return DataTypes-Wert
     */
    public static DataTypes fromFile(String fileName){
        // returns DataType from imported File
        String lower = fileName.toLowerCase();
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
            case ".mp3":
                return DataTypes.MP3;
            case ".mp4":
                return DataTypes.MP4;
            case ".xlsx":
                return DataTypes.EXCEL;
            case ".docx":
                return DataTypes.WORD;
            case ".pptx":
                return DataTypes.PPTX;
            case ".csv":
                return DataTypes.CSV;
            case ".zip":
                return DataTypes.ZIP;
            default:
                return DataTypes.NODATA;
        }
    }

    /** getExtension
     *  Gibt die Dateiendung zurück je nach Datentyp
     *
     * @param dataType aktueller Datentyp
     * @return Dateiendung als String
     */
    public static String getExtension(DataTypes dataType){
        if(dataType == DataTypes.NODATA)
            return ".bin";
        if(dataType.text.startsWith("00"))
            // sollte Dateiendung nur 2 Buchstaben haben
            return "." + dataType.text.substring(2).toLowerCase();
        if(dataType.text.startsWith("0"))
            // 0 aus dem Value entfernen und dann Dateiendung generieren (falls Dateiendung 3 Buchstaben hat)
            return "." + dataType.text.substring(1).toLowerCase();
        else
            return "." + dataType.text.toLowerCase();
    }
}