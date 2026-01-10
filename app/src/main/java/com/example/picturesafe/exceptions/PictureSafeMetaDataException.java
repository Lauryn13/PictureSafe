package com.example.picturesafe.exceptions;

public class PictureSafeMetaDataException extends PictureSafeBaseException {
    public PictureSafeMetaDataException(String message, boolean isInformation) {
        super("Metadaten konnten nicht generiert werden", message, isInformation);
    }
}
