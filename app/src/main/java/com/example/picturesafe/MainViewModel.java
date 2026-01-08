package com.example.picturesafe;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.enumerators.DataTypes;

import java.util.List;

public class MainViewModel extends ViewModel {
    public Picture[] pictures;
    public int selectedPicture;
    public boolean picturesLoaded;
    public boolean picturesHasData;
    public boolean picturesDataIsCorrupted;
    public boolean picturesAreIncomplete;
    public DataTypes storedDataType;
    public FileData fileData;
    public boolean dataChoosen;

}
