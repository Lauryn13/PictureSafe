package com.example.picturesafe.exceptions;

/** PictureSafeDataWontFitInImageException
 *  Exception die geworfen wird wenn die Daten nicht ins Bild passen
 *  Tritt auf wenn Datenbytes (MIT Signatur und Overhead für verschlüsselung etc.) größer sind als die verfügbaren Bytes in dem Bild (LSB).
 */
public class PictureSafeDataWontFitInImageException extends PictureSafeBaseException {
    public PictureSafeDataWontFitInImageException(int dataByte, int availablePictureByte) {
        super("Ausgewählte Daten zu groß für Bild.", "Die ausgewählten Daten sind zu groß für das Bild / die Bilder.\nGröße der Daten: " + dataByte + " Byte\nGröße des Bildes: " + availablePictureByte + " Byte");
    }
}
