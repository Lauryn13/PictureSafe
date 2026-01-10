package com.example.picturesafe.exceptions;

public class PictureSafeMissingPasswordException extends PictureSafeBaseException {
    public PictureSafeMissingPasswordException() {
        super("Fehlendes Passwort.", "Um das Bild verschlüsseln zu können ist ein Passwort notwendig.", true);
    }
}

