package com.example.picturesafe.exceptions;

/** PictureSafeMissingPasswordException
 *  Exception die geworfen wird wenn das Bild verschlüsselt werden soll aber kein Passwort angegeben wurde
 */
public class PictureSafeMissingPasswordException extends PictureSafeBaseException {
    public PictureSafeMissingPasswordException() {
        super("Fehlendes Passwort.", "Um das Bild verschlüsseln zu können ist ein Passwort notwendig.", true);
    }
}

