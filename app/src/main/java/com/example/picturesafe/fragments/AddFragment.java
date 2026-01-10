package com.example.picturesafe.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeCheckBox;
import com.example.picturesafe.components.PictureSafeDialog;
import com.example.picturesafe.components.PictureSafeDropDown;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.CompressionType;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeDataWontFitInImageException;
import com.example.picturesafe.exceptions.PictureSafeMissingPasswordException;
import com.example.picturesafe.exceptions.PictureSafeNotAllPicturesUsedInfo;

import java.io.IOException;
import java.util.Arrays;


public class AddFragment extends Fragment {
    private ActivityResultLauncher<Intent> pickFileLauncher;
    MainViewModel mvm;

    PictureSafeButton btnSelectFile;
    PictureSafeButton btnWrite;
    PictureSafeText fileText;
    PictureSafeEditText saveableText;
    PictureSafeDropDown compressionDropDown;
    PictureSafeCheckBox encryptionCheckBox;
    PictureSafeEditText passwordText;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pickFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                try {
                    this.load_file(result.getData().getData());
                } catch (PictureSafeBaseException e){
                    PictureSafeDialog.show(getParentFragmentManager(), e);
                }
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mvm.dataChoosen = true;

        btnSelectFile = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnSelectFile), true);
        btnWrite = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnWrite));
        fileText = new PictureSafeText(view.findViewById(R.id.fileText), view.findViewById(R.id.fileCard));
        saveableText = new PictureSafeEditText(view.findViewById(R.id.saveableText), view.findViewById(R.id.saveableCard));
        compressionDropDown = new PictureSafeDropDown(requireContext(), view.findViewById(R.id.compressionSpinner), true);
        encryptionCheckBox = new PictureSafeCheckBox(view.findViewById(R.id.textEncryption), view.findViewById(R.id.checkBoxEncrytion), true);
        passwordText = new PictureSafeEditText(view.findViewById(R.id.passwordText), view.findViewById(R.id.passwordCard));

        TextView tabFile = view.findViewById(R.id.tabFile);
        TextView tabText = view.findViewById(R.id.tabText);

        btnSelectFile.button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                    "application/pdf",
                    "image/*"
            });
            intent.setType("*/*");
            pickFileLauncher.launch(Intent.createChooser(intent, "Datei auswählen"));
            btnSelectFile.change_text("Neue Datei auswählen");
        });

        btnWrite.button.setOnClickListener(v -> {
            try {
                click_btnWrite();
                if(mvm.waitingException != null)
                    throw mvm.waitingException;
            } catch (PictureSafeBaseException e){
                PictureSafeDialog.show(getParentFragmentManager(), e);
            } finally {
                mvm.waitingException = null;
            }
        });

        encryptionCheckBox.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> passwordText.change_visibility(isChecked));

        tabFile.setOnClickListener(v -> {
            mvm.dataChoosen = true;
            btnSelectFile.change_visibility(true);
            saveableText.change_visibility(false);

            if(mvm.fileData != null){
                float storable_mb = 0;
                if(mvm.pictures != null) {
                    for (Picture pic : mvm.pictures) {
                        storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;
                    }

                    float fileLength = (float) Math.round((float) mvm.fileData.content.length / 1000 / 10) / 100;
                    fileText.setText(mvm.fileData.name + "\n\nGröße: " + fileLength + " Mb \n Maximal Speicherbare Größe: " + storable_mb + " Mb");
                    btnWrite.change_visibility(true);
                }
                else
                    fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!\n Dateiname: " + mvm.fileData.name);
            }
            else
                btnWrite.change_visibility(false);

            tabFile.setBackgroundColor(requireContext().getColor(R.color.primaryVariant));
            tabText.setBackgroundColor(requireContext().getColor(R.color.primary));
        });

        tabText.setOnClickListener(v -> {
            mvm.dataChoosen = false;
            saveableText.change_visibility(true);
            btnSelectFile.change_visibility(false);
            btnWrite.change_visibility(true);
            fileText.removeText();

            tabText.setBackgroundColor(requireContext().getColor(R.color.primaryVariant));
            tabFile.setBackgroundColor(requireContext().getColor(R.color.primary));
        });

        // load picture data and set all up
        if(mvm.pictures == null) {
            btnWrite.button.setEnabled(false);
            btnWrite.change_visibility(true);
        }

        if(mvm.fileData != null){
            float storable_mb = 0;
            if(mvm.pictures != null) {
                for (Picture pic : mvm.pictures) {
                    storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;
                }

                float fileLength = (float) Math.round((float) mvm.fileData.content.length / 1000 / 10) / 100;
                fileText.setText(mvm.fileData.name + "\n\nGröße: " + fileLength + " Mb \n Maximal Speicherbare Größe: " + storable_mb + " Mb");
            }
            else
                fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!\n Dateiname: " + mvm.fileData.name);
            btnSelectFile.change_text("Neue Datei auswählen");
            btnWrite.change_visibility(true);
        }
        else
            btnWrite.change_visibility(false);

        return view;
    }

    public void click_btnWrite(){
        // Exception handling needed
        if(!mvm.dataChoosen){
            String textData = saveableText.readText();
            mvm.fileData = new FileData(textData.getBytes(), DataTypes.TEXTDATA, "textExport");
        }
        else
            assert mvm.fileData != null;

        byte[] byteData = mvm.fileData.content;

        CompressionType compressionType = CompressionType.fromUI(encryptionCheckBox, compressionDropDown);
        char[] password = passwordText.readText().toCharArray();

        if(compressionType.uses_encryption() && password.length == 0)
            throw new PictureSafeMissingPasswordException();


        // setting data in pictures
        if(byteData.length <= mvm.pictures[0].storeable_data_in_byte){
            // Daten passen in einem Bild
            try {
                mvm.pictures[0].setData(mvm, byteData, 1, mvm.fileData.dataType, compressionType, password, mvm.fileData.name);
                mvm.pictures[0].generate_png(requireContext());
            } catch (IOException e){
                passwordText.clear_text();
                Arrays.fill(password, '\0');
                throw new RuntimeException(e);
            }

            if(mvm.pictures.length != 1)
                throw new PictureSafeNotAllPicturesUsedInfo();
        }
        else{
            // check length of FileData not longer then space in files
            int spaceInPictures = 0;
            int[] spacesPerPicture = new int[mvm.pictures.length];

            for(Picture pic : mvm.pictures) {
                spaceInPictures += pic.storeable_data_in_byte;
                spacesPerPicture[pic.currentPicture - 1] = spaceInPictures;
            }

            if(spaceInPictures < mvm.fileData.content.length)
                throw new PictureSafeDataWontFitInImageException(mvm.fileData.content.length, spaceInPictures);


            int neededPictures = 1;

            while(spacesPerPicture[neededPictures - 1] < byteData.length)
                neededPictures++;


            int offset = 0;

            for(int i = 0; i < neededPictures; i++) {
                // split Data
                int length = Math.min(mvm.pictures[i].storeable_data_in_byte, byteData.length - offset);

                byte[] saveableByteData = new byte[length];

                System.arraycopy(byteData, offset, saveableByteData, 0, length);
                try {
                    mvm.pictures[i].setData(mvm, saveableByteData, neededPictures, mvm.fileData.dataType, compressionType, password, mvm.fileData.name);
                    mvm.pictures[i].generate_png(requireContext());
                } catch (IOException e){
                    passwordText.clear_text();
                    Arrays.fill(password, '\0');
                    throw new RuntimeException(e);
                }

                offset += length;
            }

            if(mvm.pictures.length > neededPictures)
                throw new PictureSafeNotAllPicturesUsedInfo();
        }

        passwordText.clear_text();
        Arrays.fill(password, '\0');
    }

    public void load_file(Uri data) {
        if (data == null) {
            return;
        }

        try {
            mvm.fileData = new FileData(requireContext(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(mvm.pictures == null)
            fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!\n Dateiname: " + mvm.fileData.name);
        else{
            float storable_mb = 0;
            for (Picture pic : mvm.pictures) {
                storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;
            }

            float fileLength = (float) Math.round((float) mvm.fileData.content.length / 1000 / 10) / 100;
            fileText.setText(mvm.fileData.name + "\n\nGröße: " + fileLength + " Mb \n Maximal Speicherbare Größe: " + storable_mb + " Mb");
            btnWrite.change_visibility(true);
        }
    }
}