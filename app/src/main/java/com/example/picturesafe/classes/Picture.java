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
import java.util.Arrays;

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
        this(data,CompressionType.NOCCOMPRESSION , 1, 32);
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
            return true;
        }
        this.signature = PictureUtils.generate_Signature();
        return false;
    }



    /* ====== Schreiben ====== */
    private byte[] generate_metadata(int currentPicture, int dataBits){
        byte[] metadata = new byte[22];

        // currently only supports data beginning at the 2nd row
        this.rowsOfData = Math.floorDiv(dataBits / 3, this.width) + 1;
        this.lastRowDataBits = dataBits - (rowsOfData - 1) * this.width * 3 * this.k;
        Log.v(TAG, "Metadata: Data Lengths in bits" + dataBits);
        Log.v(TAG, "Metadata: row of data, last row of data: " + this.rowsOfData + " " + this.lastRowDataBits);


        byte[] dataTypeBytes = this.storedDataType.getText().getBytes();
        byte[] signatureBytes = this.signature.getBytes();
        byte[] pSafeSignatureBytes = PICTURESAFESIGNATURE.getBytes();
        byte[] compressionTypeBytes = this.compressionType.getText().getBytes();

        // TODO check lastRowDataBits are correctly calculated
        assert 0 <= this.lastRowDataBits;
        assert this.lastRowDataBits <= this.width * 3;
        assert this.rowsOfData <= 65535;
        assert this.lastRowDataBits <= 65535;
        assert this.storedDataType.getText().length() == 4;
        assert this.k <= 127;
        assert this.signature != null;
        assert this.signature.length() == 4;

        // set metadata
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

        return metadata;
    }

    // update Pixel Data with byteData (do the LSB Stuff)
    // overload return rest of byteData (Data not stored within the picture)
    // TODO need another function that sets the amount of pictures used to store data -> not possible when not every picture has been set yet
    public void setData(byte[] byteData, int currentPicture, DataTypes dataType){
        // TODO add functions and remove hard coding
        this.compressionType = CompressionType.NOCCOMPRESSION;
        this.storedDataType = dataType;

        // convert Data to bin
        int[] binData = PictureUtils.bytesToBinary(byteData);
        int pixelToUpdate = Math.ceilDiv(binData.length, 3);

        Log.v(TAG, "byteData " + Arrays.toString(byteData));
        Log.v(TAG, "binData " + Arrays.toString(binData));
        Log.v(TAG, "binDataLengths " + binData.length);
        Log.v(TAG, "pixelToUpdate " + pixelToUpdate);

        // update Pixels
        for(int i = 0; i < pixelToUpdate; i++){

            int pixelsX = Math.floorDiv(i,this.width) + 1;
            int pixelsY = i - Math.floorDiv(i, this.width) * this.width;

            int bitR = binData[i*3];
            int bitG;
            int bitB;

            try {
                bitG = binData[i * 3 + 1];
            }
            catch (ArrayIndexOutOfBoundsException e){
                bitG = 0;
            }
            try {
                bitB = binData[i * 3 + 2];
            }
            catch (ArrayIndexOutOfBoundsException e){
                bitB = 0;
            }

            this.pixels[pixelsX][pixelsY] = PictureUtils.setLSB(this.pixels[pixelsX][pixelsY], bitR, bitG, bitB);
        }

        byte[] metadata = this.generate_metadata(currentPicture, binData.length);
        assert metadata.length * 8 <= this.width * 3;

        binData = PictureUtils.bytesToBinary(metadata);

        for(int i = 0; i < Math.ceilDiv(binData.length, 3); i++) {
            int pixelsY = i - Math.floorDiv(i, this.width);

            int bitR = binData[i*3];
            int bitG;
            int bitB;

            try {
                bitG = binData[i * 3 + 1];
            }
            catch (ArrayIndexOutOfBoundsException e){
                bitG = 0;
            }
            try {
                bitB = binData[i * 3 + 2];
            }
            catch (ArrayIndexOutOfBoundsException e){
                bitB = 0;
            }

            this.pixels[0][pixelsY] = PictureUtils.setLSB(this.pixels[0][pixelsY], bitR, bitG, bitB);
        }

        this.bitmap = update_bitmap_pixels();
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

    public FileData read_content(int lenInBits, boolean readMetadata){
        int[] binData = new int[lenInBits];
        int extraRow = readMetadata ? 0 : 1;
        int pixelToRead = Math.ceilDiv(lenInBits, 3);

        for(int i = 0; i < pixelToRead; i++){
            // update Pixel ! needs function for new Pixel calculation.
            int pixelsX = Math.floorDiv(i,this.width) + extraRow;
            int pixelsY = i - Math.floorDiv(i, this.width) * this.width;

            int[] binList = PictureUtils.readLSB(this.pixels[pixelsX][pixelsY]);
            binData[i*3] = binList[0];

            try {
                binData[i*3+1] = binList[1];
            }
            catch (ArrayIndexOutOfBoundsException e){
                break;
            }

            try {
                binData[i*3+2] = binList[2];
            }
            catch (ArrayIndexOutOfBoundsException e){
                break;
            }
        }

        byte[] data = PictureUtils.binaryToBytes(binData);
        return new FileData(data, this.storedDataType, "textExport");
    }

    public FileData read_content(){
        return this.read_content((this.rowsOfData - 1) * this.width * 3 + this.lastRowDataBits, false);
    }


    private Object[] read_metadata(){
        byte[] data = this.read_content(22 * 8, true).content;
        return PictureUtils.convertMetaDataBytes(data);
    }
}
