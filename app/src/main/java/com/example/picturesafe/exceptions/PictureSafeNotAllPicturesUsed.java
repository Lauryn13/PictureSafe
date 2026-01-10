package com.example.picturesafe.exceptions;

public class PictureSafeNotAllPicturesUsed extends PictureSafeBaseException {
    public PictureSafeNotAllPicturesUsed() {
        super("Nicht alle Bilder wurden genutzt", "Die ausgewählten Daten haben nicht alle ausgewählten Bilder als Speicherort gebraucht. Nur die genutzten Bilder wurden erneut erstellt.", true);
    }
}
