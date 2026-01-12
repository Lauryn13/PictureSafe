package com.example.picturesafe.exceptions;

/** PictureSafePictureTooSmallException
 *  Exception die geworfen wird wenn das Bild zu klein ist um die Daten speichern zu können.
 *  Das Bild muss mindestens 72 Pixel Breite haben,  um alle Metadaten richtig Speichern zu können.
 *  Das Bild muss mindestens 2 Pixel Länge haben um auch Daten speichern zu können (1 Zeile immer Metadaten)
 */
public class PictureSafePictureTooSmallException extends PictureSafeBaseException {
    public PictureSafePictureTooSmallException(int width, int height) {
        super("Ausgewähltes Bild ist zu klein.", "Um Daten im Bild speichern zu können, wird eine mindestgröße des Bildes gebraucht.\nMinimale Bildgröße: 72x2 Pixel\nAktuelle Bildgröße: " + width + "x" + height + " Pixel");
    }
}
