package com.example.picturesafe.classes;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.picturesafe.enumerators.DataTypes;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileData extends StoringData<String>{
    public byte[] content;
    public DataTypes dataType;
    public String name;

    public FileData(Context context, Uri uri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // block for reading
        byte[] tmp = new byte[4096];
        int n;

        while ((n = is.read(tmp)) != -1){
            baos.write(tmp, 0, n);
        }

        is.close();


        this.name = getFileName(context, uri);
        this.dataType = DataTypes.fromFile(this.name);
        this.content = baos.toByteArray();
    }
    // Overload for import
    public FileData(byte[] content, DataTypes dataType, String name){
        this.content = content;
        this.dataType = dataType;
        this.name = name;
    }

    public static String getFileName(Context context, Uri uri) {
        String name = null;

        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null);

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex);
            }
            cursor.close();
        }

        return name;
    }

    @Override
    public byte[] convert_to_bytes(){
        return this.content;
    }
}
