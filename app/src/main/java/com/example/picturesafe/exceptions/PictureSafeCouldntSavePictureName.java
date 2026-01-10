package com.example.picturesafe.exceptions;

public class PictureSafeCouldntSavePictureName extends PictureSafeBaseException {
    public PictureSafeCouldntSavePictureName() {
        super("Bildname konnte nicht gespeichert werden", "Das Bild ist zu schmal, um den Namen der zu enthaltenen Daten speichern zu können.");
    }
}
