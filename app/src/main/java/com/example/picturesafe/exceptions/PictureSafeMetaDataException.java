package com.example.picturesafe.exceptions;

/** PictureSafeMetaDataException
 *  Exception die geworfen wird wenn die Metadaten nicht generiert werden konnten
 *  Grund wird mit angegeben, hat damit zu tun dass die zu speichernden Werte zu groß sind um in die dafür vorgesehenden X-Bytes in den Metadaten zu speichern.
 */
public class PictureSafeMetaDataException extends PictureSafeBaseException {
    public PictureSafeMetaDataException(String message, boolean isInformation) {
        super("Metadaten konnten nicht generiert werden", message, isInformation);
    }
}
