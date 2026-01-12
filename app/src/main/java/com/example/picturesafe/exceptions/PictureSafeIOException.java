package com.example.picturesafe.exceptions;

/** PictureSafeIOException
 *  Exception die geworfen wird wenn ein Fehler beim Lesen oder Schreiben auftritt
 */
public class PictureSafeIOException extends PictureSafeBaseException {
    public PictureSafeIOException(Throwable e) {
        super("Dateifehler","Die Datei konnte nicht gelesen oder gespeichert werden.", e);
    }
}
