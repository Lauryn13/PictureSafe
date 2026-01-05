package com.example.picturesafe.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.DataTypes;

import java.io.InputStream;
import java.util.Arrays;


public class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";

    MainViewModel mvm;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    PictureSafeButton btnSelectPicture;
    PictureSafeImage imageView;
    PictureSafeText infoText;
    PictureSafeButton btnReset;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                this.loadImage(result.getData().getData());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        this.btnSelectPicture = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnSelect), true);
        this.btnReset = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnReset));
        this.imageView = new PictureSafeImage(view.findViewById(R.id.imageView), view.findViewById(R.id.imageCard), true);
        this.infoText = new PictureSafeText(view.findViewById(R.id.infoText), view.findViewById(R.id.infoCard));

        btnSelectPicture.button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(Intent.createChooser(intent, "Bild auswählen"));
        });

        btnReset.button.setOnClickListener(v -> {
            imageView.removeImage();
            infoText.removeText();

            btnReset.change_visibility(false);

            mvm.picture = null;
            mvm.fileData = null;
        });

        // Load Picturedata if possible and set all up
        if(mvm.picture != null){
            // TODO add more Metadata read while getting the picture.
            imageView.setImage(mvm.picture.bitmap);
            infoText.setText(mvm.picture.generate_info_text());
            btnReset.change_visibility(true);
        }

        return view;
    }


    private void loadImage(Uri data){
        if(data == null){
            return;
        }

        // Bild auswählen
        try {
            // Bild als Bitmap laden
            // Nullable abfangen!
            InputStream inputStream = requireContext().getContentResolver().openInputStream(data);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            mvm.picture = new Picture(bitmap);
            imageView.setImage(mvm.picture.bitmap);

            if(mvm.picture.hasData){
                if(mvm.picture.compressionType.uses_encryption()){
                    showPasswordDialog(password -> {
                        Log.v(TAG, "Password from Edit: " + Arrays.toString(password));
                        this.read_file_data(password);
                        // Passwort nach nutzung überschreiben
                        Arrays.fill(password, '0');
                    });
                    Log.v(TAG, "continue reading after Password input.");
                }
                else{
                    this.read_file_data();
                }
            }

            infoText.setText(mvm.picture.generate_info_text());
            btnReset.change_visibility(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void read_file_data(char[] password){
        mvm.fileData = mvm.picture.read_content(password);

        if(mvm.picture.storedDataType == DataTypes.JPG){
            Bitmap outputBitmap = BitmapFactory.decodeByteArray(mvm.fileData.content, 0, mvm.fileData.content.length);
        }
        else if(mvm.picture.storedDataType == DataTypes.TEXTDATA){
            String text = new String(mvm.fileData.content);
        }
    }
    private void read_file_data(){
        this.read_file_data(null);
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
                .setNegativeButton("Abbrechen", (d, w) -> {
                    pwEdit.clear_text();
                })
                .show();
    }
}