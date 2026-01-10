package com.example.picturesafe.exceptions;

public class PictureSafeNotAllPicturesUsedInfo extends PictureSafeBaseException{
    public PictureSafeNotAllPicturesUsedInfo() {
        super("Nicht alle Bilder wurden genutzt", "Die ausgewählten Daten haben nicht alle ausgewählten Bilder als Speicherort gebraucht. Nur die genutzten Bilder wurden erneut erstellt.", true);
    }
}
