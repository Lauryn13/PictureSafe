package com.example.picturesafe.exceptions;

/** PictureSafeNotAllPicturesUsedInfo
 *  Exception die geworfen wird wenn nicht alle Bilder genutzt wurden
 *  Tritt auf wenn im Bild mehrere Bilder als Speichergrundlage für die Datei angegeben wurden als der Nutzer ausgewählt hat.
 */
public class PictureSafeNotAllPicturesUsedInfo extends PictureSafeBaseException{
    public PictureSafeNotAllPicturesUsedInfo() {
        super("Nicht alle Bilder wurden genutzt", "Die ausgewählten Daten haben nicht alle ausgewählten Bilder als Speicherort gebraucht. Nur die genutzten Bilder wurden erneut erstellt.", true);
    }
}
