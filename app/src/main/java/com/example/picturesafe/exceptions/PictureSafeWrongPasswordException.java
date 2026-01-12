package com.example.picturesafe.exceptions;

/** PictureSafeWrongPasswordException
 *  Exception die geworfen wird wenn das Passwort falsch ist
 *  Kann auch geworfen werden, sollten andere Probleme bei der Generierung des Schlüssels auftreten.
 */
public class PictureSafeWrongPasswordException extends PictureSafeBaseException {
    public PictureSafeWrongPasswordException() {
        super("Falsches Passwort", "Das angegebene Passwort kann die Daten nicht entschlüsseln, versuche es erneut.");
    }
}
