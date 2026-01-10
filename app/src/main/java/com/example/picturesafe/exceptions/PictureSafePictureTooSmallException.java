package com.example.picturesafe.exceptions;

public class PictureSafePictureTooSmallException extends PictureSafeBaseException {
    public PictureSafePictureTooSmallException(int width, int height) {
        super("Ausgewähltes Bild ist zu klein.", "Um Daten im Bild speichern zu können, wird eine mindestgröße des Bildes gebraucht.\nMinimale Bildgröße: 72x2 Pixel\nAktuelle Bildgröße: " + width + "x" + height + " Pixel");
    }
}
