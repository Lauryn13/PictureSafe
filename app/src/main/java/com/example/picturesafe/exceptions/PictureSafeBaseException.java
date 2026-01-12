package com.example.picturesafe.exceptions;

/** PictureSafeBaseException
 *  Dient als Grundlage für die Verschiedenen Exceptions bzw. Informations in diesem Projekt.
 *
 *  Wird gebraucht um aus dieser Exception das Dialogfenster erstellen zu können.
 */
public class PictureSafeBaseException extends RuntimeException {
    public final String message;
    public final String description;
    public final boolean isInformation; // Entscheidet über das Icon des Dialogfensters

    /** Konstruktor
     *  Konstruktor für die Exceptionens, indem Message und Beschreibung gesetzt werden.
     *
     * @param message Message der Exception -> dient eher als Titel
     * @param description Beschreibung der Exception
     * @param baseException Eigentliche Exception, kann zum loggen später noch genutzt werden
     * @param isInformation ob die Exception eine Information ist -> Ändert das Icon
     */
    public PictureSafeBaseException(String message, String description, Throwable baseException, boolean isInformation) {
        super(message, baseException);
        this.message = message;
        this.description = description;
        this.isInformation = isInformation;
    }

    /** Konstruktor-Overload
     *  Konstruktor für die Exceptionens, indem Message und Beschreibung gesetzt werden.
     *
     * @param message Message der Exception -> dient eher als Titel
     * @param description Beschreibung der Exception
     * @param isInformation ob die Exception eine Information ist -> Ändert das Icon
     */
    public PictureSafeBaseException(String message, String description, boolean isInformation) {
        super(message);
        this.message = message;
        this.description = description;
        this.isInformation = isInformation;
    }

    /** Konstruktor-Overload
     *  Konstruktor für die Exceptionens, indem Message und Beschreibung gesetzt werden.
     *
     * @param message Message der Exception -> dient eher als Titel
     * @param description Beschreibung der Exception
     * @param baseException Eigentliche Exception, kann zum loggen später noch genutzt werden
     */
    public PictureSafeBaseException(String message, String description, Throwable baseException){
        this(message, description, baseException, false);
    }

    /** Konstruktor-Overload
     *  Konstruktor für die Informations
     *
     * @param message Message der Information -> dient eher als Titel
     * @param description Beschreibung der Information
     */
    public PictureSafeBaseException(String message, String description){
        this(message, description, false);
    }
}
