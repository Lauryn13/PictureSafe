package com.example.picturesafe.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeDialog;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeDataMissingException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;


public class SavedFragment extends Fragment {
    private static final String TAG = "SavedFragment";

    MainViewModel mvm;

    PictureSafeImage outputImage;
    PictureSafeButton btnExport;
    PictureSafeButton btnDecrypt;
    PictureSafeText readedText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        this.btnExport = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnExport));
        this.btnDecrypt = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnDecrypt));
        this.readedText = new PictureSafeText(view.findViewById(R.id.readedText), view.findViewById(R.id.readedCard));
        this.outputImage = new PictureSafeImage(view.findViewById(R.id.outputImage), view.findViewById(R.id.outputCard));

        this.btnExport.button.setOnClickListener(v -> {
            try {
                click_btnExport();
            } catch (PictureSafeBaseException e){
                PictureSafeDialog.show(getParentFragmentManager(), e);
            }
        });

        this.btnDecrypt.button.setOnClickListener(v -> {
            try {
                click_btnDecrypt();
            } catch (PictureSafeBaseException e){
                PictureSafeDialog.show(getParentFragmentManager(), e);
            }
        });

        showImage();
        return view;
    }

    public void click_btnExport(){
        if(mvm.fileData != null){
            Uri uri;
            try {
                uri = mvm.fileData.export_file(getContext());
            } catch (IOException e){
                throw new RuntimeException(e);
            }
            Objects.requireNonNull(uri);
        } else
            throw new PictureSafeDataMissingException("Es wurden keine Daten zum abspeichern im Bild angegeben.");
    }

    public void click_btnDecrypt(){
        showPasswordDialog(password -> {
            try {
                this.read_file_data(password);
                showImage();
            } catch (PictureSafeBaseException e) {
                PictureSafeDialog.show(getParentFragmentManager(), e);
            }
            // Passwort nach nutzung überschreiben
            Arrays.fill(password, '0');
        });
    }

    public void showImage(){
        this.btnDecrypt.change_visibility(false);
        if(mvm.pictures == null)
            this.readedText.setText("Bitte wähle zunächst ein Foto zum lesen aus.");
        else if(mvm.picturesAreIncomplete)
            this.readedText.setText("Zum wiederherstellen der Daten sind nicht alle Bilder vorhanden.");
        else if (mvm.picturesHasData && mvm.picturesDataIsCorrupted)
            this.readedText.setText("Die Inhalte des Bildes wurden überschrieben und dadurch zerstört.");
        else if(mvm.picturesHasData && mvm.fileData == null) {
            this.readedText.setText("Der gespeicherte Inhalt ist zurzeit noch Verschlüsselt.");
            this.btnDecrypt.change_visibility(true);
        }
        else if(!mvm.picturesHasData)
            this.readedText.setText("Dieses Bild hat keinen gespeicherten Inhalt.");
        else{
            switch (mvm.storedDataType){
                case JPG:
                case PNG:
                    Bitmap outputBitmap = BitmapFactory.decodeByteArray(mvm.fileData.content, 0, mvm.fileData.content.length);
                    outputImage.setImage(outputBitmap);
                    readedText.setText(mvm.fileData.name);
                    break;
                case TEXTDATA:
                    String text = new String(mvm.fileData.content);
                    readedText.setText(text);
                    break;
                default:
                    Log.v(TAG, "Unsupported DataType");
                    readedText.setText(mvm.fileData.name + "\n\nBisher nicht unterstützter Datentyp. Er kann hier zwar nicht angezeigt, jedoch exportiert werden.");
                    break;
            }

            if(mvm.storedDataType != DataTypes.TEXTDATA)
                this.btnExport.change_visibility(true);
        }
    }

    private void read_file_data(char[] password){
        // Crashed bei Passwortcheck und dann nur unvollständige anzahl an Bildern
        String name = null;
        byte[][] data = new byte[mvm.pictures.length][];
        int completeDataLength = 0;
        int picturesWithData = 0;
        String signature = null;

        for(int i = 0; i < mvm.pictures.length; i++){
            Picture picture = mvm.pictures[i];
            if(picture.hasData){
                // read current picture
                // check signature
                if(signature != null && !signature.equals(picture.signature)){
                    continue;
                }
                else if(signature == null)
                    // at this Point only this hidden file is read no matter if other pictures with other files exist
                    signature = picture.signature;

                Log.v("ImageFragement", "currentPicture: " + picture.currentPicture);
                try {
                    data[picture.currentPicture - 1] = picture.read_content(password).content;
                } catch (IndexOutOfBoundsException e){
                    mvm.picturesAreIncomplete = true;
                    return;
                }
                name = picture.name;
                completeDataLength += picture.savedDataLength;
                picturesWithData = picture.amountOfPictures;
            }
        }

        byte[] completeData = new byte[completeDataLength];

        Log.v("Image", "picWithData: "+picturesWithData);
        Log.v("Image", "data.length: "+data.length);
        if(picturesWithData > data.length){
            mvm.picturesAreIncomplete = true;
            return;
        }

        for(int i = 0; i < picturesWithData; i++){
            if(data[i] == null){
                mvm.picturesAreIncomplete = true;
                return;
            }
        }
        int pos = 0;
        for(byte[] b : data){
            if(b == null)
                break;

            System.arraycopy(b, 0, completeData, pos, b.length);
            pos += b.length;
        }

        mvm.fileData = new FileData(completeData, mvm.storedDataType, name);
    }

    private void showPasswordDialog(Consumer<char[]> onPassword) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.password_dialog, null);
        PictureSafeEditText pwEdit = new PictureSafeEditText(view.findViewById(R.id.passwordText), view.findViewById(R.id.passwordCard), true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Verschlüsselte Daten")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Entschlüsseln", (d, w) -> {
                    char[] pw = pwEdit.readText().toCharArray();
                    onPassword.accept(pw);
                    pwEdit.clear_text();
                })
                .setNegativeButton("Abbrechen", (d, w) -> pwEdit.clear_text())
                .show();
    }
}