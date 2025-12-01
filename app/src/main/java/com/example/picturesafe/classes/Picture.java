package com.example.picturesafe.classes;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.*;
import java.util.Arrays;

public class Picture {
    private static final String TAG = "Picture";

    public Bitmap bitmap;
    public int height;
    public int width;
    public int k; // Number of used LSB-Bits
    public int s; // Bits used for Signature
    public int storeable_data_in_kb;

    private int[][] pixels;

    public Picture(Bitmap data, int k, int s){
        // Constructor
        this.bitmap = data;

        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
        this.k = k;
        this.s = s;

        this.storeable_data_in_kb = ((width * (height-1)*3*k) - (s+16)*(height-1)) / 8000;
        this.pixels = this.read_pixel_array();
    }

    // Overload for standard values (optional parameters)
    public Picture(Bitmap data){
        this(data, 1, 32);
    }

    // decoding of bitmap into Bytes-Array (changeable data)
    private int[][] read_pixel_array(){
        int[][] pixels = new int[this.height][this.width];

        for(int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                pixels[y][x] = this.bitmap.getPixel(x, y);
            }
        }
//        Log.v(TAG, Integer.toString(pixels.length));
//        int argb = pixels[0][0];
//        Log.d("PIXEL", String.format("0x%08X", argb));

        // check lengths are correct: Exception handling needed!
        assert pixels.length == this.height;
        assert pixels[0].length == this.width;
        return pixels;
    }

    private Bitmap update_bitmap_pixels(){
        Bitmap newBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newBitmap.setPixel(x, y, pixels[y][x]);
            }
        }
        return newBitmap;
    }

//    public File generate_png(){
//        File file = new File(getFilesDir(), "output.png");
//        FileOutputStream fos = new FileOutputStream(file);
//
//        this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//        fos.close();
//        return file;
//    }
}
