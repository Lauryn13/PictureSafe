package com.example.picturesafe;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.classes.TextData;

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
    private static final int PICK_IMAGE = 1;
    ImageView imageView;
    TextView infoText;
    TextView readedText;
    EditText saveableText;

    Picture picture;
    TextData textData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelect = findViewById(R.id.btnSelect);
        imageView = findViewById(R.id.imageView);
        infoText = findViewById(R.id.infoText);
        readedText = findViewById(R.id.readedText);

        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE);
        });

        saveableText = (EditText) findViewById(R.id.saveableText);
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

                picture = new Picture(bitmap);
                imageView.setImageBitmap(picture.bitmap);

                if(picture.hasData){
                    TextData text = picture.read_content();
                    readedText.setText(text.content);
                }

                infoText.setText(
                        "Auflösung: " + picture.width + " x " + picture.height +
                                "\nSpeicherbare Datenmenge: " + picture.storeable_data_in_kb + " KiloBytes"
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void click_btnWrite(View v){
        textData = new TextData(saveableText.getText().toString());
        Log.v(TAG, "Text to safe " + saveableText.getText().toString());

        byte[] binData = textData.convert_to_bytes();
        picture.setData(binData, 0);

        imageView.setImageBitmap(picture.bitmap);
        try {
            Uri uri = picture.generate_png(getBaseContext());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}