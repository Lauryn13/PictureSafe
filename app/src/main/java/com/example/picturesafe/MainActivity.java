package com.example.picturesafe;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeLayout;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.DataTypes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    PictureSafeImage imageView;
    PictureSafeImage outputImage;

    PictureSafeButton btnSelectPicture;
    PictureSafeButton btnSelectFile;
    PictureSafeButton btnReset;
    PictureSafeButton btnWrite;
    PictureSafeButton btnExport;

    PictureSafeText infoText;
    PictureSafeText fileText;
    PictureSafeText readedText;

    PictureSafeEditText saveableText;

    PictureSafeLayout insertDataLayout;
    PictureSafeLayout outputDataLayout;

    Picture picture;
    FileData fileData;

    byte[] byteData;
    boolean dataChoosen;

    // Request Codes
    static final int PICK_IMAGE = 1;
    static final int PICK_FILE  = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tabFile = findViewById(R.id.tabFile);
        TextView tabText = findViewById(R.id.tabText);

        insertDataLayout = new PictureSafeLayout(findViewById(R.id.insertDataLayout));
        outputDataLayout = new PictureSafeLayout(findViewById(R.id.outputDataLayout));

        btnSelectPicture = new PictureSafeButton(getBaseContext(), findViewById(R.id.btnSelect), true);
        btnSelectFile = new PictureSafeButton(getBaseContext(), findViewById(R.id.btnSelectFile));
        btnWrite = new PictureSafeButton(getBaseContext(), findViewById(R.id.btnWrite));
        btnExport = new PictureSafeButton(getBaseContext(), findViewById(R.id.btnExport));
        btnReset = new PictureSafeButton(getBaseContext(), findViewById(R.id.btnReset));

        fileText = new PictureSafeText(findViewById(R.id.fileText), findViewById(R.id.fileCard));
        infoText = new PictureSafeText(findViewById(R.id.infoText), findViewById(R.id.infoCard));
        readedText = new PictureSafeText(findViewById(R.id.readedText), findViewById(R.id.readedCard));

        imageView = new PictureSafeImage(findViewById(R.id.imageView), findViewById(R.id.imageCard), true);
        outputImage = new PictureSafeImage(findViewById(R.id.outputImage), findViewById(R.id.outputCard));

        saveableText = new PictureSafeEditText(findViewById(R.id.saveableText), findViewById(R.id.saveableCard));

        dataChoosen = true;

        btnSelectPicture.button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE);
        });

        btnSelectFile.button.setOnClickListener(v -> {
            Log.v(TAG, "btnSelectFile clicked");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                    "application/pdf",
                    "image/*"
            });
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FILE);
        });

        btnReset.button.setOnClickListener(v -> {
            imageView.removeImage();
            outputImage.removeImage();
            infoText.removeText();
            fileText.removeText();
            readedText.removeText();
            outputDataLayout.change_visibility(false);
            insertDataLayout.change_visibility(false);

            btnSelectPicture.set_highlight(true);
            btnReset.change_visibility(false);
            btnExport.change_visibility(false);
            btnWrite.change_visibility(false);

            this.picture = null;
            this.fileData = null;
        });

        tabFile.setOnClickListener(v -> {
            dataChoosen = true;
            btnSelectFile.change_visibility(true);
            saveableText.change_visibility(false);

            tabFile.setBackgroundColor(getColor(R.color.primaryVariant));
            tabText.setBackgroundColor(getColor(R.color.primary));
        });

        tabText.setOnClickListener(v -> {
            dataChoosen = false;
            saveableText.change_visibility(true);
            btnSelectFile.change_visibility(false);

            tabText.setBackgroundColor(getColor(R.color.primaryVariant));
            tabFile.setBackgroundColor(getColor(R.color.primary));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null){
            return;
        }
        Uri uri = data.getData();

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            // Bild auswählen
            try {
                // Bild als Bitmap laden
                // Nullable abfangen!
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                picture = new Picture(bitmap);
                imageView.setImage(picture.bitmap);

                if(picture.hasData){
                    fileData = picture.read_content();

                    if(picture.storedDataType == DataTypes.JPG){
                        Bitmap outputBitmap = BitmapFactory.decodeByteArray(fileData.content, 0, fileData.content.length);
                        outputImage.setImage(outputBitmap);
                    }
                    else if(picture.storedDataType == DataTypes.TEXTDATA){
                        String text = new String(fileData.content);
                        readedText.setText(text);
                    }
                    outputDataLayout.change_visibility(true);
                    readedText.setText(fileData.name);
                    btnExport.set_highlight(true);
                    btnExport.change_visibility(true);
                }

                infoText.setText(
                        "Auflösung: " + picture.width + " x " + picture.height +
                                "\nSpeicherbare Datenmenge: " + picture.storeable_data_in_kb + " KiloBytes"
                );

               insertDataLayout.change_visibility(true);
               btnSelectPicture.set_highlight(false);
               btnSelectFile.change_visibility(true);
               btnWrite.change_visibility(true);
               btnReset.change_visibility(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == PICK_FILE && resultCode == RESULT_OK){
            // Datei auswählen
            try {
                fileData = new FileData(getBaseContext(), uri);
                fileText.setText(fileData.name);
                Log.v(TAG, "FILE WAS READED.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(TAG, "FILE READING THREW AN ERROR.");
            }
        }
    }

    public void click_btnWrite(View v){
        // Exception handling needed
        if(!dataChoosen){
            String textData = saveableText.readText();
            fileData = new FileData(textData.getBytes(), DataTypes.TEXTDATA, "textExport");
            byteData = fileData.convert_to_bytes();
        }
        else{
            byteData = fileData.convert_to_bytes();
        }

        if(fileData.name != null){
            picture.setData(byteData,0, fileData.dataType, fileData.name);
        }
        else{
            picture.setData(byteData, 0, fileData.dataType);
        }
        imageView.setImage(picture.bitmap);
        try {
            Uri uri = picture.generate_png(getBaseContext());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void click_btnExport(View v){
        if(this.fileData != null){
            Log.v(TAG, "EXPORTING FILE");
            try {
                Uri uri = fileData.export_file(getBaseContext());
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