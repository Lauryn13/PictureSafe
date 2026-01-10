package com.example.picturesafe.exceptions;

public class PictureSafeDataMissingException extends PictureSafeBaseException {
    public PictureSafeDataMissingException(String description) {
        super("Fehlende Daten", description, true);
    }
}
