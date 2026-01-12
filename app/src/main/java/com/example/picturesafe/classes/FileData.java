package com.example.picturesafe.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeFileNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/** FileData
 * Stellt die in einem Bild gespeicherten Daten als Klasse zur Verfügung.
 * Kümmert sich daher um:
 *  - Umwandlung der Daten vom File in ein Byte-Array und umgekehrt.
 *  - Lesen des Ursprünglichen Dateinamen.
 */
public class FileData{
    /** Aktuelle Datei als Byte-Array **/
    public byte[] content;
    /** Aktuell gespeicherter Dateityp **/
    public DataTypes dataType;
    /** Dateiname **/
    public String name;

    /** Konstruktor
     * Erstellt ein FileData-Objekt aus einer Datei.
     *
     * @param context Context der Aktuellen Activity
     * @param uri URI der zu lesenden Datei
     * @throws IOException Bei Fehlern beim Lesen der Datei
     */
    public FileData(Context context, Uri uri) throws IOException {
        InputStream is;

        try {
            is = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            throw new PictureSafeFileNotFoundException();
        }
        Objects.requireNonNull(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int n;

        // Lesen der Datei einzelnen Blöcken
        while ((n = is.read(tmp)) != -1)
            baos.write(tmp, 0, n);

        is.close();

        this.name = getFileName(context, uri);
        this.dataType = DataTypes.fromFile(this.name);
        this.content = baos.toByteArray();
    }

    /** Konstruktor Overload
     * Erstellt ein FileData-Objekt aus den gelesenen Daten eines Bildes.
     *
     * @param content Byte-Array der Daten
     * @param dataType ursprünglicher Datentyp
     * @param name ursprünglicher Name der Datei
     */
    public FileData(byte[] content, DataTypes dataType, String name){
        this.content = content;
        this.dataType = dataType;
        this.name = name;
    }

    /** exportFile
     * Exportiert das FileData-Objekt als Datei.
     *
     * @param context Context der Aktuellen Activity
     * @return URI der exportierten Datei
     * @throws IOException Bei Fehlern beim Lesen/Schreiben
     */
    public Uri exportFile(Context context) throws IOException{
        Uri collection;
        String mime;
        String path;
        OutputStream out;

        // Erstellung der Datei benötigt je nach Dateityp unterschiedliche Informationen
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
                return null;
        }

        // Zusammenfügen der Informationen für die Erstellung der eigentlichen Datei
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, this.name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, path);
        Uri uri = context.getContentResolver().insert(collection, values);
        Objects.requireNonNull(uri);

        try {
            out = context.getContentResolver().openOutputStream(uri);
        } catch(FileNotFoundException e){
            throw new PictureSafeFileNotFoundException();
        }
        Objects.requireNonNull(out);

        // Erstellen der Datei -> Unterscheidung zwischen Bild und "Datei".
        if (dataType == DataTypes.PNG || dataType == DataTypes.JPG) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
            bitmap.compress(dataType == DataTypes.PNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
        } else
            out.write(content);

        out.close();
        return uri;
    }

    /** getFileName
     * Liest den Dateinamen aus einer URI aus.
     *
     * Lösung angelehnt an: https://stackoverflow.com/questions/13275007/contentresolver-how-to-get-file-name-from-uri
     *
     * @param context Context der Aktuellen Activity
     * @param uri URI der zu lesenden Datei
     * @return Dateiname als String
     */
    public static String getFileName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        Objects.requireNonNull(cursor);

        // Index, an welcher Stelle der Dateiname steht
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        String name = null;

        // Lesen des Namens
        if (cursor.moveToFirst())
            name = cursor.getString(nameIndex);
        cursor.close();

        return name;
    }
}
