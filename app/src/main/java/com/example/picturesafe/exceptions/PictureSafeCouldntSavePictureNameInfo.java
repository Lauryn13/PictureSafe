package com.example.picturesafe.exceptions;

/** PictureSafeCouldntSavePictureNameInfo
 *  Exception die geworfen wird wenn der Bildname nicht gespeichert werden kann
 *  Tritt auf, wenn das Bild indem gespeichert wird zu schmal ist um den gesamten Namen noch speichern zu können, ohne in die 2 Zeile zu gehen.
 */
public class PictureSafeCouldntSavePictureNameInfo extends PictureSafeBaseException{
    public PictureSafeCouldntSavePictureNameInfo() {
        super("Bildname konnte nicht gespeichert werden", "Das Bild ist zu schmal, um den Namen der zu enthaltenen Daten speichern zu können.", true);
    }
}
