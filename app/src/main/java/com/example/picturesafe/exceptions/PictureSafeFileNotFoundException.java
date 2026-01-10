package com.example.picturesafe.exceptions;

public class PictureSafeFileNotFoundException extends PictureSafeBaseException {
    public PictureSafeFileNotFoundException() {
        super("Datei wurde nicht gefunden", "Die Ausgewählte Datei wurde nicht gefunden.");
    }
}
