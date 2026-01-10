package com.example.picturesafe.exceptions;

public class PictureSafeWrongPasswordException extends PictureSafeBaseException {
    public PictureSafeWrongPasswordException() {
        super("Falsches Passwort", "Das angegebene Passwort kann die Daten nicht entschlüsseln, versuche es erneut.");
    }
}
