package com.example.picturesafe.exceptions;

/** PictureSafeSecurityNotSupported
 *  Exception die geworfen wird wenn die sicherheitsrelevanten Anforderungen nicht unterstützt werden.
 */
public class PictureSafeSecurityNotSupported extends PictureSafeBaseException{
    public PictureSafeSecurityNotSupported() {
        super("Sicherheitsfeatures nicht unterstützt", "Das Gerät unterstützt die gebrauchten sicherheitsrelevanten Anforderungen nicht.");
    }
}
