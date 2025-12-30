package com.example.picturesafe.classes;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.picturesafe.enumerators.CompressionType;
import com.example.picturesafe.enumerators.DataTypes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Picture {
    private static final String TAG = "Picture";
    private static final String PICTURESAFESIGNATURE = "PSafe";

    public Bitmap bitmap;
    public String name;
    public int height;
    public int width;
    public int k; // Number of used LSB-Bits
    public String signature; // Signature of the picture

    public int storeable_data_in_kb;
    public boolean hasData;
    public CompressionType compressionType;

    private int[][] pixels;
    public DataTypes storedDataType;
    private int rowsOfData;
    private int lastRowDataBits;



    /* ====== Initialisierung ====== */
    public Picture(Bitmap data, CompressionType compressionType, int k, int s){
        // Constructor
        this.bitmap = data;

        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
        this.k = k;

        this.storeable_data_in_kb = ((width * (height-1)*3*k) - (s+16)*(height-1)) / 8000;
        this.pixels = this.read_pixel_array();

        this.hasData = this.check_for_data();
    }

    // Overload for standard values (optional parameters)
    public Picture(Bitmap data){
        this(data,CompressionType.NOCOMPRESSION , 1, 32);
    }
    public Picture(Bitmap data, CompressionType compressionType) {
        this(data, compressionType, 1, 32);
    }

    private boolean check_for_data(){
        Object[] metadata = this.read_metadata();

        if(metadata[0].equals(PICTURESAFESIGNATURE)){
            this.rowsOfData = (int) metadata[3];
            this.lastRowDataBits = (int) metadata[4];
            this.storedDataType = DataTypes.fromText(metadata[5].toString());
            this.k = (int) metadata[6];
            this.signature = metadata[7].toString();
            this.compressionType = CompressionType.fromText(metadata[8].toString());
            this.name = metadata[10].toString();
            return true;
        }
        this.signature = PictureUtils.generate_Signature();
        return false;
    }



    /* ====== Schreiben ====== */
    private byte[] generate_metadata(int currentPicture, int dataBits, String name){
        byte[] metadata = new byte[23];

        // currently only supports data beginning at the 2nd row
        this.rowsOfData = Math.floorDiv(dataBits / 3, this.width) + 1;
        this.lastRowDataBits = dataBits - (rowsOfData - 1) * this.width * 3 * this.k;
        int nameBytes = name != null ? name.getBytes().length : 0;

        Log.v(TAG, "Metadata: Data Lengths in bits" + dataBits);
        Log.v(TAG, "Metadata: row of data, last row of data: " + this.rowsOfData + " " + this.lastRowDataBits);

        byte[] dataTypeBytes = this.storedDataType.getText().getBytes();
        byte[] signatureBytes = this.signature.getBytes();
        byte[] pSafeSignatureBytes = PICTURESAFESIGNATURE.getBytes();
        byte[] compressionTypeBytes = this.compressionType.getText().getBytes();

        assert 0 <= this.lastRowDataBits;
        assert this.lastRowDataBits <= this.width * 3;
        assert this.rowsOfData <= 65535;
        assert this.lastRowDataBits <= 65535;
        assert this.storedDataType.getText().length() == 4;
        assert this.k <= 127;
        assert this.signature != null;
        assert this.signature.length() == 4;
        assert nameBytes <= 255;

        // set metadata in first 22 Bytes
        metadata[0] = pSafeSignatureBytes[0];
        metadata[1] = pSafeSignatureBytes[1];
        metadata[2] = pSafeSignatureBytes[2];
        metadata[3] = pSafeSignatureBytes[3];
        metadata[4] = pSafeSignatureBytes[4];
        metadata[5] = (byte) 0; // amount of Pictures (can not be set here yet)
        metadata[6] = (byte) currentPicture; // maximum 127 Pictures -> exception handling needed
        metadata[7] = (byte) (this.rowsOfData >> 8); // Erster Byte
        metadata[8] = (byte) this.rowsOfData; // 2 Byte
        metadata[9] = (byte) (this.lastRowDataBits >> 8); // Erster Byte
        metadata[10] = (byte) this.lastRowDataBits; // 2 Byte
        metadata[11] = dataTypeBytes[0]; // DataType of saved Data
        metadata[12] = dataTypeBytes[1];
        metadata[13] = dataTypeBytes[2];
        metadata[14] = dataTypeBytes[3];
        metadata[15] = (byte) this.k; // LSB Bits
        metadata[16] = signatureBytes[0];
        metadata[17] = signatureBytes[1];
        metadata[18] = signatureBytes[2];
        metadata[19] = signatureBytes[3];
        metadata[20] = compressionTypeBytes[0];
        metadata[21] = compressionTypeBytes[1];
        metadata[22] = (byte) nameBytes;

        return metadata;
    }
    public byte[] generate_metadata(int currentPicture, int dataBits){
        return this.generate_metadata(currentPicture, dataBits, null);
    }

    // update Pixel Data with byteData (do the LSB Stuff)
    // overload return rest of byteData (Data not stored within the picture)
    // TODO need another function that sets the amount of pictures used to store data -> not possible when not every picture has been set yet
    public void setData(byte[] byteData, int currentPicture, DataTypes dataType, String name){
        // TODO add functions and remove hard coding
        this.compressionType = CompressionType.NOCOMPRESSION;
        this.storedDataType = dataType;

        // convert Data to bin
        int[] binData = PictureUtils.bytesToBinary(byteData);
        int pixelToUpdate = Math.ceilDiv(binData.length, 3);

        int bitIndex = 0;

        int pixelsX = 1; // Zeile 0 = Metadata
        int pixelsY = 0;

        Log.v(TAG, "binDataLengths " + binData.length);
        Log.v(TAG, "pixelToUpdate " + pixelToUpdate);

        // update Pixels
        for(int i = 0; i < pixelToUpdate; i++){

            int bitR = binData[bitIndex++];
            int bitG = (bitIndex < binData.length) ? binData[bitIndex++] : 0;
            int bitB = (bitIndex < binData.length) ? binData[bitIndex++] : 0;

            pixels[pixelsX][pixelsY] = PictureUtils.setLSB(pixels[pixelsX][pixelsY], bitR, bitG, bitB);

            if (++pixelsY == width) {
                pixelsY = 0;
                pixelsX++;
            }
        }

        // Metadaten generieren
        byte[] metadata = this.generate_metadata(currentPicture, binData.length, name);
        byte[] nameBytes = name.getBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(metadata);
            out.write(nameBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.v(TAG, "Name written: " + name);
        Log.v(TAG, "Name Bytes: " + Arrays.toString(name.getBytes()));

        int[] metaBin = PictureUtils.bytesToBinary(out.toByteArray());
        bitIndex = 0;
        pixelsY = 0;

        // Metadaten schreiben
        while (bitIndex < metaBin.length) {
            int bitR = metaBin[bitIndex++];
            int bitG = (bitIndex < metaBin.length) ? metaBin[bitIndex++] : 0;
            int bitB = (bitIndex < metaBin.length) ? metaBin[bitIndex++] : 0;

            pixels[0][pixelsY] = PictureUtils.setLSB(pixels[0][pixelsY], bitR, bitG, bitB);

            pixelsY++;
        }

        this.bitmap = update_bitmap_pixels();
    }
    public void setData(byte[] byteData, int currentPicture, DataTypes dataType){
        this.setData(byteData, currentPicture, dataType, null);
    }


    public void setAmountofPictures(int amountofPictures){
        // TODO
    }


    /* ====== Export ====== */
    private Bitmap update_bitmap_pixels(){
        Bitmap newBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newBitmap.setPixel(x, y, this.pixels[y][x]);
            }
        }
        return newBitmap;
    }

    public Uri generate_png(Context context) throws IOException {
        // maybe use FileData for Export? -> might not be faster so use this instead

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



    /* ====== Lesen ====== */
    // decoding of bitmap into Bytes-Array (changeable data)
    private int[][] read_pixel_array(){
        int[][] pixels = new int[this.height][this.width];
        int[] flat = new int[width * height];

        // lesen der Pixel in ein 1D Array
        this.bitmap.getPixels(flat, 0, width, 0, 0, width, height);

        // umwandeln der Pixel in ein 2D Array
        for(int y = 0, i = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++, i++) {
                pixels[y][x] = flat[i];
            }
        }
        return pixels;
    }

    public FileData read_content(int lenInBits, boolean readMetadata, int offsetBits) {

        int bitOffset = offsetBits % 3;
        int startPixel = offsetBits / 3;

        byte[] binData = new byte[lenInBits];

        int extraRow = readMetadata ? 0 : 1;

        int pixelsX = startPixel / width + extraRow;
        int pixelsY = startPixel % width;

        int bi = 0;

        while (bi < lenInBits) {
            int pixel = pixels[pixelsX][pixelsY];

            // Reihenfolge: R, G, B
            int[] bits = {
                    (pixel >> 16) & 1,
                    (pixel >> 8)  & 1,
                    pixel & 1
            };

            for (int c = bitOffset; c < 3 && bi < lenInBits; c++) {
                binData[bi++] = (byte) bits[c];
            }

            bitOffset = 0; // nur beim ersten Pixel relevant

            if (++pixelsY == width) {
                pixelsY = 0;
                pixelsX++;
            }
        }

        // check signature
//        if(!readMetadata){
//            binData = PictureUtils.remove_check_signature(binData, this.width, PictureUtils.bytesToBinary(this.signature.getBytes()));
//            if(binData == null){
//                return null;
//            }
//        }

        byte[] data = PictureUtils.binaryToBytes(binData);
        return new FileData(data, this.storedDataType, this.name);
    }
    public FileData read_content(){
        return this.read_content((this.rowsOfData - 1) * this.width * 3 + this.lastRowDataBits, false, 0);
    }
    public FileData read_content(int lenInBits, boolean readMetadata){
        return this.read_content(lenInBits, readMetadata, 0);
    }


    private Object[] read_metadata(){
        byte[] data = this.read_content(23 * 8, true).content;
        int nameBytes = data[22] & 0xFF;
        String name = new String(this.read_content(nameBytes * 8, true, 23*8).content);
        Log.v(TAG, "name Length: " + nameBytes);
        Log.v(TAG, "saved FileName: " + name);
        Log.v(TAG, "data: " + Arrays.toString(data));
        return PictureUtils.convertMetaDataBytes(data, name);
    }
}
