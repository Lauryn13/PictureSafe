package com.example.picturesafe.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.picturesafe.enumerators.DataTypes;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileData{
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

    public Uri export_file(Context context) throws IOException {
        Uri collection;
        String mime;
        String path;

        switch (this.dataType){
            case PNG:
                mime = "image/png";
                path = "DCIM/PictureSafe";
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case JPG:
                mime = "image/jpeg";
                path = "DCIM/PictureSafe";
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case TXTDATA:
                mime = "text/plain";
                path = "Documents/PictureSafe";
                collection = MediaStore.Files.getContentUri("external");
                break;
            case PDF:
                mime = "application/pdf";
                path = "Documents/PictureSafe";
                collection = MediaStore.Files.getContentUri("external");
                break;
            default:
                Log.v("FileData", "NO FILETYPE FOUND");
                return null;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, this.name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, path);

        Uri uri = context.getContentResolver().insert(collection, values);
        assert uri != null;
        OutputStream out = context.getContentResolver().openOutputStream(uri);
        assert out != null;

        if (dataType == DataTypes.PNG || dataType == DataTypes.JPG) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
            bitmap.compress(dataType == DataTypes.PNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
        } else {
            out.write(content);
        }

        out.close();
        return uri;
    }

    public byte[] convert_to_bytes(){
        return this.content;
    }
}
