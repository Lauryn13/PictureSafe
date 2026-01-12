package com.example.picturesafe.exceptions;

/** PictureSafeDataCorruptedInfo
 *  Exception die geworfen wird wenn die Daten im Bild nicht wiederhergestellt werden können
 *  Hat verschiedene Gründe, meist überschriebene Daten und dann folgende Probleme beim Dekomprimieren
*/
public class PictureSafeDataCorruptedInfo extends PictureSafeBaseException{
    public PictureSafeDataCorruptedInfo() {
        super("Korrupte Daten im Bild", "Die im Bild gespeicherten Daten wurden (teilweise) überschrieben und sind daher nicht wiederherstelltbar.", true);
    }
}
