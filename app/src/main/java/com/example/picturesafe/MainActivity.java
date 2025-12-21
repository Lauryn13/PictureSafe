package com.example.picturesafe;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.classes.TextData;
import com.example.picturesafe.enumerators.DataTypes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    ImageView imageView;
    ImageView outputImage;
    TextView infoText;
    TextView fileText;
    TextView readedText;
    EditText saveableText;

    Picture picture;
    FileData fileData;
    TextData textData;

    byte[] binData;

    // Request Codes
    static final int PICK_IMAGE = 1;
    static final int PICK_FILE  = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelect = findViewById(R.id.btnSelect);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        fileText = findViewById(R.id.fileText);

        imageView = findViewById(R.id.imageView);
        outputImage = findViewById(R.id.outputImage);
        infoText = findViewById(R.id.infoText);
        readedText = findViewById(R.id.readedText);

        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE);
        });

        btnSelectFile.setOnClickListener(v -> {
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

        saveableText = (EditText) findViewById(R.id.saveableText);
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
                imageView.setImageBitmap(picture.bitmap);

                if(picture.hasData){
                    FileData fileData = picture.read_content();

                    if(picture.storedDataType == DataTypes.JPG){
                        Bitmap outputBitmap = BitmapFactory.decodeByteArray(fileData.content, 0, fileData.content.length);
                        outputImage.setImageBitmap(outputBitmap);
                    }
                    else if(picture.storedDataType == DataTypes.TEXTDATA){
                        String text = new String(fileData.content);
                        readedText.setText(text);
                    }
                }

                infoText.setText(
                        "Auflösung: " + picture.width + " x " + picture.height +
                                "\nSpeicherbare Datenmenge: " + picture.storeable_data_in_kb + " KiloBytes"
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == PICK_FILE && resultCode == RESULT_OK){
            // Datei auswählen
            try {
                fileData = new FileData(getBaseContext(), uri);
                binData = fileData.convert_to_bytes();
                fileText.setText(fileData.name);
                Log.v(TAG, "FILE WAS READED.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(TAG, "FILE READING THREW AN ERROR.");
            }
        }
    }

    public void click_btnWrite(View v){
//        textData = new TextData(saveableText.getText().toString());
//        Log.v(TAG, "Text to safe " + saveableText.getText().toString());
//
//        binData = textData.convert_to_bytes();
//        picture.setData(binData, 0, DataTypes.TEXTDATA);
        Log.v(TAG, "Name " + fileData.name);
        Log.v(TAG, "FileType " + fileData.dataType);

        picture.setData(binData, 0, fileData.dataType);

        imageView.setImageBitmap(picture.bitmap);
        try {
            Uri uri = picture.generate_png(getBaseContext());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}