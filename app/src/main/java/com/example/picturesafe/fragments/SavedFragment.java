package com.example.picturesafe.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeText;

import java.io.IOException;


public class SavedFragment extends Fragment {
    private static final String TAG = "SavedFragment";

    MainViewModel mvm;

    PictureSafeImage outputImage;
    PictureSafeButton btnExport;
    PictureSafeText readedText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        this.btnExport = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnExport));
        this.readedText = new PictureSafeText(view.findViewById(R.id.readedText), view.findViewById(R.id.readedCard));
        this.outputImage = new PictureSafeImage(view.findViewById(R.id.outputImage), view.findViewById(R.id.outputCard));

        this.btnExport.button.setOnClickListener(v -> {
            click_btnExport();
        });

        // Load Picturedata if possible and set all up

        if(mvm.pictures == null)
            this.readedText.setText("Bitte wähle zunächst ein Foto zum lesen aus.");
        else if(mvm.picturesAreIncomplete)
            this.readedText.setText("Zum wiederherstellen der Daten sind nicht alle Bilder vorhanden.");
        else if (mvm.picturesHasData && mvm.picturesDataIsCorrupted)
            this.readedText.setText("Die Inhalte des Bildes wurden überschrieben und dadurch zerstört.");
        else if(!mvm.picturesHasData || mvm.fileData == null)
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

            this.btnExport.change_visibility(true);
        }

        return view;
    }

    public void click_btnExport(){
        if(mvm.fileData != null){
            Log.v(TAG, "EXPORTING FILE");
            try {
                Uri uri = mvm.fileData.export_file(getContext());
                Log.v(TAG, "URI: " + uri.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            Log.v(TAG, "NO FILEDATA");
        }
    }
}