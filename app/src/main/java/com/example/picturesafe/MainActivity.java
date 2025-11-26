package com.example.picturesafe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    ImageView imageView;
    TextView infoText;

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                // Bild als Bitmap laden
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();


                // Anzeige
                imageView.setImageBitmap(bitmap);

                // --- Auflösung ---
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();


                int k = 1;
                int s = 32;
                int capacity = ((width * (height-1)*3*k) - (s+16)*(height-1)) / 8000;

                infoText.setText(
                        "Auflösung: " + width + " x " + height +
                                "\nSpeicherbare Datenmenge: " + capacity + " KiloBytes"
                );


                // Hier hast du das Bitmap-Objekt für Steganographie
                // z.B.: this.loadedBitmap = bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}