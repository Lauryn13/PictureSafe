package com.example.picturesafe.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.classes.PictureUtils;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeDialog;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeFileNotFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ImageFragment extends Fragment {

    MainViewModel mvm;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    PictureSafeButton btnSelectPicture;
    PictureSafeImage imagePreview;
    PictureSafeText infoText;
    PictureSafeButton btnReset;
    LinearLayout thumbnailContainer;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                List<Uri> uris = new ArrayList<>();

                if (result.getData().getClipData() != null) {
                    ClipData clip = result.getData().getClipData();
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        uris.add(clip.getItemAt(i).getUri());
                    }
                } else {
                    uris.add(result.getData().getData());
                }

                Uri[] uriArray = uris.toArray(new Uri[0]);

                try{
                    this.loadImages(uriArray);
                } catch (PictureSafeBaseException e){
                    PictureSafeDialog.show(getParentFragmentManager(),e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                this.showImages();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        this.btnSelectPicture = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnSelect), true);
        this.btnReset = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnReset));
        this.infoText = new PictureSafeText(view.findViewById(R.id.infoText), view.findViewById(R.id.infoCard));
        this.thumbnailContainer = view.findViewById(R.id.thumbnailContainer);
        this.imagePreview = new PictureSafeImage(view.findViewById(R.id.mainImage), view.findViewById(R.id.imageCard));

        btnSelectPicture.button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImageLauncher.launch(Intent.createChooser(intent, "Bild auswählen"));
        });

        btnReset.button.setOnClickListener(v -> {
            imagePreview.removeImage();
            infoText.removeText();

            btnReset.change_visibility(false);

            mvm.pictures = null;
            mvm.fileData = null;
        });

        // Load Picturedata if possible and set all up
        if(mvm.pictures != null){
            showImages(mvm.selectedPicture);
            infoText.setText(PictureUtils.generate_info_text(mvm.pictures, mvm.selectedPicture));
            btnReset.change_visibility(true);
        }

        return view;
    }

    void showImages(int selectedPicture) {
        thumbnailContainer.removeAllViews();

        for (int i = 0; i < mvm.pictures.length; i++) {
            Bitmap bitmap = mvm.pictures[i].bitmap;

            ImageView thumb = new ImageView(requireContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(160, 160);
            lp.setMargins(8, 0, 8, 0);

            thumb.setLayoutParams(lp);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setImageBitmap(bitmap);

            int pictureIndex = i;
            thumb.setOnClickListener(v -> {
                imagePreview.setImage(bitmap);
                mvm.selectedPicture = pictureIndex;
                infoText.setText(PictureUtils.generate_info_text(mvm.pictures, mvm.selectedPicture));
            });

            thumbnailContainer.addView(thumb);

            // erstes Bild direkt anzeigen
            if (i == selectedPicture) {
                imagePreview.setImage(bitmap);
            }
        }
    }
    void showImages(){
        this.showImages(0);
    }

    private void loadImages(Uri[] uris) throws IOException {
        Picture[] pictures = new Picture[uris.length];

        boolean hasData = false;
        boolean dataIsCorrupted = false;
        DataTypes storedDataType = null;
        boolean usesEncrytion = false;
        String signature = PictureUtils.generate_signature();

        // Bild als Bitmap laden
        // Nullable abfangen!
        for(int i = 0; i < uris.length; i++){
            InputStream inputStream;
            try {
                inputStream = requireContext().getContentResolver().openInputStream(uris[i]);
            } catch (FileNotFoundException e){
                throw new PictureSafeFileNotFoundException();
            }
            Objects.requireNonNull(inputStream);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            pictures[i] = new Picture(bitmap, i+1, signature);

            if (pictures[i].hasData) {
                hasData = true;
                storedDataType = pictures[i].storedDataType;
                if(!dataIsCorrupted)
                    dataIsCorrupted = pictures[i].dataIsCorrupted;
                if(pictures[i].compressionType.uses_encryption())
                    usesEncrytion = true;
            }
        }

        mvm.pictures = pictures;
        mvm.picturesLoaded = true;
        mvm.picturesAreIncomplete = false;
        mvm.picturesHasData = hasData;
        mvm.picturesDataIsCorrupted = dataIsCorrupted;
        mvm.selectedPicture = 0;
        mvm.storedDataType = storedDataType;

        if(hasData){
            if(usesEncrytion){
                showPasswordDialog(password -> {
                    try {
                        this.read_file_data(password);
                    } catch (PictureSafeBaseException e) {
                        PictureSafeDialog.show(getParentFragmentManager(), e);
                    }
                    // Passwort nach nutzung überschreiben
                    Arrays.fill(password, '0');
                });
            }
            else{
                this.read_file_data();
            }
        }

        infoText.setText(PictureUtils.generate_info_text(mvm.pictures, mvm.selectedPicture));
        btnReset.change_visibility(true);
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
                .setNegativeButton("Abbrechen", (d, w) -> pwEdit.clear_text())
                .show();
    }
}