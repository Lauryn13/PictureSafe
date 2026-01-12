package com.example.picturesafe.exceptions;

/** PictureSafeFileNotFoundException
 *  Exception die geworfen wird wenn die Datei nicht gefunden wird
 */
public class PictureSafeFileNotFoundException extends PictureSafeBaseException {
    public PictureSafeFileNotFoundException() {
        super("Datei wurde nicht gefunden", "Die Ausgewählte Datei wurde nicht gefunden.");
    }
}
