package com.example.picturesafe.exceptions;

public class PictureSafeCouldntSavePictureNameInfo extends PictureSafeBaseException{
    public PictureSafeCouldntSavePictureNameInfo() {
        super("Bildname konnte nicht gespeichert werden", "Das Bild ist zu schmal, um den Namen der zu enthaltenen Daten speichern zu können.", true);
    }
}
