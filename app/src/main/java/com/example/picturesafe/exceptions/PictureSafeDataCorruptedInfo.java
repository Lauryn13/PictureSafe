package com.example.picturesafe.exceptions;

public class PictureSafeDataCorruptedInfo extends PictureSafeBaseException{
    public PictureSafeDataCorruptedInfo() {
        super("Korrupte Daten im Bild", "Die im Bild gespeicherten Daten wurden (teilweise) überschrieben und sind daher nicht wiederherstelltbar.", true);
    }
}
