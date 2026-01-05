package com.example.picturesafe.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeCheckBox;
import com.example.picturesafe.components.PictureSafeDropDown;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeLayout;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.CompressionType;
import com.example.picturesafe.enumerators.DataTypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class AddFragment extends Fragment {
    private static final String TAG = "SavedFragment";
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
                this.load_file(result.getData().getData());
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
            Log.v(TAG, "btnSelectFile clicked");
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
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });

        encryptionCheckBox.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordText.change_visibility(true);
                } else {
                    passwordText.change_visibility(false);
                }
            }
        });

        tabFile.setOnClickListener(v -> {
            mvm.dataChoosen = true;
            btnSelectFile.change_visibility(true);
            saveableText.change_visibility(false);

            if(mvm.fileData != null){
                fileText.setText(mvm.fileData.name);
                btnWrite.change_visibility(true);
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
        if(mvm.picture == null) {
            btnWrite.button.setEnabled(false);
            btnWrite.change_visibility(true);
        }

        if(mvm.fileData != null){
            fileText.setText(mvm.fileData.name);
            btnSelectFile.change_text("Neue Datei auswählen");
            btnWrite.change_visibility(true);
        }
        else
            btnWrite.change_visibility(false);


        return view;
    }

    public void click_btnWrite() throws NoSuchAlgorithmException {
        // Exception handling needed
        if(!mvm.dataChoosen){
            String textData = saveableText.readText();
            mvm.fileData = new FileData(textData.getBytes(), DataTypes.TEXTDATA, "textExport");
        }

        byte[] byteData = mvm.fileData.convert_to_bytes();

        CompressionType compressionType = CompressionType.fromUI(encryptionCheckBox, compressionDropDown);
        Log.v(TAG, "compressionType returns as: " + compressionType.text);
        char[] password = passwordText.readText().toCharArray();

        if(mvm.fileData.name != null){
            mvm.picture.setData(byteData,0, mvm.fileData.dataType, compressionType, password, mvm.fileData.name);
        }
        else{
            mvm.picture.setData(byteData, 0, mvm.fileData.dataType, compressionType, password);
        }
        try {
            Uri uri = mvm.picture.generate_png(requireContext());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load_file(Uri data) {
        if (data == null) {
            return;
        }

        try {
            mvm.fileData = new FileData(requireContext(), data);
            fileText.setText(mvm.fileData.name);
            btnWrite.change_visibility(true);
            Log.v(TAG, "FILE WAS READED.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "FILE READING THREW AN ERROR.");
        }
    }
}