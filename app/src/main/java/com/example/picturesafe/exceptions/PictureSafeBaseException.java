package com.example.picturesafe.exceptions;

public class PictureSafeBaseException extends RuntimeException {
    public final String message;
    public final String description;
    public final boolean isInformation;

    public PictureSafeBaseException(String message, String description, Throwable baseException, boolean isInformation) {
        super(message, baseException);
        this.message = message;
        this.description = description;
        this.isInformation = isInformation;
    }
    public PictureSafeBaseException(String message, String description, boolean isInformation) {
        super(message);
        this.message = message;
        this.description = description;
        this.isInformation = isInformation;
    }
    public PictureSafeBaseException(String message, String description, Throwable baseException){
        this(message, description, baseException, false);
    }
    public PictureSafeBaseException(String message, String description){
        this(message, description, false);
    }
}
