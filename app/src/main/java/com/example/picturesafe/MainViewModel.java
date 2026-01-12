package com.example.picturesafe;

import androidx.lifecycle.ViewModel;

import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;

/** MainViewModel
 *  ViewModel zum speichern verschiedener Informationen zwischen den Fragmenten
 *  Ermöglicht steuerung der UI, je nach vorhandenen Daten.
 */
public class MainViewModel extends ViewModel {
    // Bilder, die Daten enthalten könnten
    public Picture[] pictures;

    // Informationen zu den geladenen Bildern
    public int selectedPicture;
    public boolean picturesLoaded;
    public boolean picturesHasData;
    public boolean picturesDataIsCorrupted;
    public boolean picturesAreIncomplete;

    // gespeicherte Datei in den Bildern
    public DataTypes storedDataType;
    public FileData fileData;

    // UI Elemente
    public boolean dataChoosen;

    // Exception während des Schreibens, die aber nicht direkt geworfen werden sollen sondern zu geeigneten Zeitpunkten
    public PictureSafeBaseException waitingException;

}
