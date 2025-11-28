package com.example.picturesafe.classes;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class Picture {
    public Bitmap bitmap;
    public int height;
    public int width;
    public int k; // Number of used LSB-Bits
    public int s; // Bits used for Signature
    public int storeable_data_in_kb;

    public Picture(Bitmap data, int k, int s){
        // Constructor
        this.bitmap = data;

        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
        this.k = k;
        this.s = s;

        this.storeable_data_in_kb = ((width * (height-1)*3*k) - (s+16)*(height-1)) / 8000;
    }

    // Overload for standard values (optional parameters)
    public Picture(Bitmap data){
        this(data, 1, 32);
    }

    // decoding of bitmap into Bytes-Array (changeable data)
    private byte[] read_byte_array(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        return stream.toByteArray();
    }
}
