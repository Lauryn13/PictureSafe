package com.example.picturesafe.exceptions;

public class PictureSafeSecurityNotSupported extends PictureSafeBaseException{
    public PictureSafeSecurityNotSupported() {
        super("Sicherheitsfeatures nicht unterstützt", "Das Gerät unterstützt die gebrauchten sicherheitsrelevanten Anforderungen nicht.");
    }
}
