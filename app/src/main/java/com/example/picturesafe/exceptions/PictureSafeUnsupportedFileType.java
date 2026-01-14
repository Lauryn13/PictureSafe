package com.example.picturesafe.exceptions;

public class PictureSafeUnsupportedFileType extends PictureSafeBaseException{
    public PictureSafeUnsupportedFileType() {
        super("Nicht unterstütztes Dateiformat.","Das aktuell ausgewählte Dateiformat wird nicht unterstützt. Bitte wähle eine andere Datei zum speichern aus.");
    }
}
