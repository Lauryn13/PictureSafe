package com.example.picturesafe.enumerators;

import com.example.picturesafe.classes.Compression;
import com.example.picturesafe.components.PictureSafeCheckBox;
import com.example.picturesafe.components.PictureSafeDropDown;

import java.io.IOException;
import java.util.zip.Deflater;

/** CompressionType
 *  Enum um Optionen für Kompression und Verschlüsselung zu speichern
 *  IMMER 2 Buchstaben lang -> für Speicherung in den Metadaten:
 *    - 1. Buchstabe = Komprimierungsalgorithmus
 *    - 2. Buchstabe = AES-Verschlüsselung
 *
 *  Reihenfolge der Umwandlungen: Daten <-> Komprimierung <-> Verschlüsselung
 */
public enum CompressionType {
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

    /** fromText
     *  Konvertiert den String aus den Metadaten in einen CompressionType-Wert
     *
     * @param text String der umgewandelt werden soll
     * @return CompressionType-Wert
     */
    public static CompressionType fromText(String text) {
        for (CompressionType dt : values())
            if (dt.text.equals(text))
                return dt;

        return NO;
    }

    /** usesEncryption
     *  Gibt an ob die Daten mit AES verschlüsselt werden soll
     *
     * @return Werden Daten verschlüsselt?
     */
    public boolean usesEncryption() {
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

    /** fromUI
     *  Konvertiert die Optionen aus dem Frontend in einen CompressionType-Wert
     *
     * @param checkBox Checkbox für die Verschlüsselung
     * @param dropDown Dropdown für den Komprimierungsalgorithmus
     * @return CompressionType-Wert
     */
    public static CompressionType fromUI(PictureSafeCheckBox checkBox, PictureSafeDropDown dropDown){
        CompressionType compType;
        CompressionDropdown comDropDown = dropDown.getSelectedItem();

        // Erstmal Verschlüsselung mit rein
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
        // Wenn Verschlüsselung nicht aktiv ist wieder entfernen
        if(!checkBox.isChecked())
            return compType.removeAes();
        else
            return compType;
    }

    /** removeCompression
     *  Entfernt die Kompression (Bspw. wenn Komprimierung länger ist als eigentliche Daten)
     *
     * @return CompressionType ohne Kompression
     */
    public CompressionType removeCompression(){
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

    /** removeAES
     *  Entfernt die Verschlüsselung
     *
     * @return CompressionType ohne AES
     */
    public CompressionType removeAes() {
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

    /** compressData
     *  Komprimiert die Daten je nach ausgewählten Algorithmus
     *
     * @param data zu komprimierenden Daten
     * @return komprimierte Daten oder Null, sollte keine Komprimierung aktiv sein
     * @throws IOException Fehler beim Schreiben/Lesen
     */
    public byte[] compressData(byte[] data) throws IOException {
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

    /** decompressData
     *  Entkomprimiert die Daten je nach ausgewählten Algorithmus
     *
     * @param data zu entkomprimierenden Daten
     * @param originalSize Größe der zu entkomprimierenden Daten
     * @return entkomprimierte Daten
     */
    public byte[] decompressData(byte[] data, int originalSize){
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
