package com.example.picturesafe.exceptions;

public class PictureSafeOutOfMemory extends PictureSafeBaseException{
    public PictureSafeOutOfMemory() {
        super("Arbeitsspeicher voll!", "Beim Laden der Daten ist der Arbeitsspeicher ausgegangen. Versuchen Sie weniger Bilder oder kleinere Datein zu nutzen.");
    }
}
