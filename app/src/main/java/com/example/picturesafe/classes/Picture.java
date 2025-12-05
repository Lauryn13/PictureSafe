package com.example.picturesafe.classes;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Picture {
    private static final String TAG = "Picture";

    public Bitmap bitmap;
    public String name;
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

        // check lengths are correct: Exception handling needed!
        assert pixels.length == this.height;
        assert pixels[0].length == this.width;
        return pixels;
    }

    // update Pixel Data with byteData (do the LSB Stuff)
    public void setData(byte[] byteData){
        // convert Data to bin
        int[] binData = this.bytesToBinary(byteData);
        Log.v(TAG, Arrays.toString(binData));
        int bitsToUpdate = Math.ceilDiv(binData.length, 3);
        Log.v(TAG, bitsToUpdate + "");
        Log.v(TAG, this.pixels.length + "px Lenght");
        Log.v(TAG, this.pixels[0].length + "px Width");


        // update Pixels
        for(int i = 0; i < bitsToUpdate; i++){
            // update Pixel ! needs function for new Pixel calculation.
            int pixelsX = Math.floorDiv(i,this.pixels[0].length);
            int pixelsY = i - Math.floorDiv(i, this.pixels[0].length);

            int bitR = binData[i*3];
            int bitG;
            int bitB;

            if (i*3+1 >= binData.length)
                bitG = 0;
            else
                bitG = binData[i*3+1];

            if (i*3+2 >= binData.length)
                bitB = 0;
            else
                bitB = binData[i*3+2];

            Log.v(TAG, Integer.toString(pixelsX));
            Log.v(TAG, Integer.toString(i));


            this.pixels[pixelsX][pixelsY] = this.setLSB(this.pixels[pixelsX][pixelsY], bitR, bitG, bitB);
        }

        this.bitmap = update_bitmap_pixels();
    }

    public Uri generate_png(Context context) throws IOException {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "output.png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/PictureSafe");

        Uri uri = context.getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        assert uri != null;
        OutputStream out = context.getContentResolver().openOutputStream(uri);

        assert out != null;
        this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();

        return uri;
    }

    public TextData read_content(int len){
        len = len * 8;
        int bits_to_read = Math.ceilDiv(len,3);
        int[] binData = new int[len];

        for(int i = 0; i < bits_to_read; i++){
            // update Pixel ! needs function for new Pixel calculation.
            int pixelsX = Math.floorDiv(i,this.pixels[0].length);
            int pixelsY = i - Math.floorDiv(i, this.pixels[0].length);

            int[] binList = this.readLSB(this.pixels[pixelsX][pixelsY]);
            binData[i*3] = binList[0];

            if(i*3+1 >= len)
                break;
            else if(i*3+1 < len)
                binData[i*3+1] = binList[1];

            if(i*3+2 >= len)
                break;
            else
                binData[i*3+2] = binList[2];
        }

        byte[] data = this.binaryToBytes(binData);
        String str = new String(data, StandardCharsets.UTF_8);
        return new TextData(str);
    }

    // Pivate helper functions

    private int setLSB(int pixel, int bitR, int bitG, int bitB) {
        int a = (pixel >> 24) & 0xFF;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8)  & 0xFF;
        int b =  pixel        & 0xFF;

        r = (r & ~1) | (bitR & 1);
        g = (g & ~1) | (bitG & 1);
        b = (b & ~1) | (bitB & 1);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int[] readLSB(int pixel){
        int r = ((pixel >> 16) & 0xFF) % 2;
        int g = ((pixel >> 8)  & 0xFF) % 2;
        int b = (pixel         & 0xFF) % 2;

        return new int[] {r,g,b};
    }


    private Bitmap update_bitmap_pixels(){
        Bitmap newBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newBitmap.setPixel(x, y, this.pixels[y][x]);
            }
        }
        return newBitmap;
    }

    private int[] bytesToBinary(byte[] bytes) {
        int[] bits = new int[bytes.length * 8];
        int index = 0;

        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                bits[index++] = (b >> i) & 1;
            }
        }
        return bits;
    }

    private byte[] binaryToBytes(int[] binData){
        int byteLength = (binData.length + 7) / 8;
        byte[] result = new byte[byteLength];

        for (int i = 0; i < binData.length; i++) {
            int byteIndex = i / 8;
            result[byteIndex] <<= 1;
            result[byteIndex] |= (binData[i] & 1);
        }

        // Letztes Byte ggf. auffüllen, falls nicht 8 Bits
        int remainder = binData.length % 8;
        if (remainder != 0) {
            result[byteLength - 1] <<= (8 - remainder);
        }

        return result;
    }
}
