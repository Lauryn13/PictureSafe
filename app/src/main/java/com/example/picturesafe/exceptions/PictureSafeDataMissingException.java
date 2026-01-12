package com.example.picturesafe.exceptions;

/** PictureSafeDataMissingException
 *  Exception die geworfen wird wenn nicht alle Daten im Bild vorhanden sind
 *  Beispielsweise wenn keine Datei zum speichern ausgewählt wurde.
 */
public class PictureSafeDataMissingException extends PictureSafeBaseException {
    public PictureSafeDataMissingException(String description) {
        super("Fehlende Daten", description, true);
    }
}
