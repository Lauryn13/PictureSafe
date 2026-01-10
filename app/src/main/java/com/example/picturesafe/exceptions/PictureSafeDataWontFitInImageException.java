package com.example.picturesafe.exceptions;

public class PictureSafeDataWontFitInImageException extends PictureSafeBaseException {
    public PictureSafeDataWontFitInImageException(int dataByte, int availablePictureByte) {
        super("Ausgewählte Daten zu groß für Bild.", "Die ausgewählten Daten sind zu groß für das Bild / die Bilder.\nGröße der Daten: " + dataByte + " Byte\nGröße des Bildes: " + availablePictureByte + " Byte");
    }
}
