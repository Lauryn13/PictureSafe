package com.example.picturesafe.exceptions;

public class PictureSafeIOException extends PictureSafeBaseException {
    public PictureSafeIOException(Throwable e) {
        super("Dateifehler","Die Datei konnte nicht gelesen oder gespeichert werden.", e);
    }
}
