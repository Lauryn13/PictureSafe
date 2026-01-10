package com.example.picturesafe.exceptions;

public class PictureSafeDataCorruptedException extends PictureSafeBaseException {
    public PictureSafeDataCorruptedException() {
        super("Korrupte Daten im Bild", "Die im Bild gespeicherten Daten wurden (teilweise) überschrieben und sind daher nicht wiederherstelltbar.");
    }
}
