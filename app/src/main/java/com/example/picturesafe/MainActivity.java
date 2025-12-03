package com.example.picturesafe;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.classes.TextData;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PICK_IMAGE = 1;
    ImageView imageView;
    TextView infoText;
    EditText saveableText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelect = findViewById(R.id.btnSelect);
        imageView = findViewById(R.id.imageView);
        infoText = findViewById(R.id.infoText);

        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE);
        });

        EditText saveableText = (EditText) findViewById(R.id.saveableText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                // Bild als Bitmap laden
                // Nullable abfangen!
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                imageView.setImageBitmap(bitmap);

                Picture picture = new Picture(bitmap);
                String text = "Hallo!";

                TextData textData = new TextData(text);
                binary[] binData = textData.convert_to_binary();

                infoText.setText(
                        "Auflösung: " + picture.width + " x " + picture.height +
                                "\nSpeicherbare Datenmenge: " + picture.storeable_data_in_kb + " KiloBytes"
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}