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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Picture {
    private static final String TAG = "Picture";
    private static final String PICTURESAFESIGNATURE = "PSafe";

    public Bitmap bitmap;
    public String name;
    public int height;
    public int width;
    public int amountOfPictures;
    public int currentPicture;
    public int k; // Number of used LSB-Bits
    public String signature; // Signature of the picture

    public int storeable_data_in_byte;
    public boolean hasData;
    public boolean dataIsCorrupted;
    public CompressionType compressionType;

    private int[][] pixels;
    public DataTypes storedDataType;
    public int savedDataLength;
    private int rowsOfData; // Zeilen in denen Daten gespeichert wurden, inkludiert die letzte (evt. nicht volle Reihe)
    private int lastRowDataBits; // Anzahl der Bits die in der letzten Spalte gespeichert wurden (nicht Index!!)



    /* ====== Initialisierung ====== */
    public Picture(Bitmap data, int currentPicture, String signature, int k){
        // Constructor
        this.bitmap = data;

        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
        this.k = k;
        this.currentPicture = currentPicture;

        this.storeable_data_in_byte = ((width * (height-1) * 3 * k) - (32 + 16) * (height - 1)) / 8;
        this.pixels = this.read_pixel_array();

        this.hasData = this.check_for_data(signature);
        // TODO set in reading
        this.dataIsCorrupted = false;
    }

    // Overload for standard values (optional parameters)
    public Picture(Bitmap data, int currentPicture, String signature){
        this(data, currentPicture, signature, 1);
    }

    private boolean check_for_data(String signature){
        Object[] metadata = this.read_metadata();

        if(metadata[0].equals(PICTURESAFESIGNATURE)){
            this.amountOfPictures = (int) metadata[1];
            this.currentPicture = (int) metadata[2];
            this.rowsOfData = (int) metadata[3];
            this.lastRowDataBits = (int) metadata[4];
            this.storedDataType = DataTypes.fromText(metadata[5].toString());
            this.k = (int) metadata[6];
            this.signature = metadata[7].toString();
            this.compressionType = CompressionType.fromText(metadata[8].toString());
            this.savedDataLength = (int) metadata[9];
            this.name = metadata[11].toString();

            Log.v(TAG, "Signature: " + this.signature);
            Log.v(TAG, "CompressionType: " + this.compressionType.text);
            return true;
        }
        this.signature = signature;

        Log.v(TAG, "amountOfPictures: " + this.amountOfPictures);
        Log.v(TAG, "currentPicture: " + this.currentPicture);

        return false;
    }

    public String generate_info_text(){
        return  "Auflösung: " + this.width + " x " + this.height +
                "\nSpeicherbare Datenmenge: " + this.storeable_data_in_byte + " KiloBytes";
    }



    /* ====== Schreiben ====== */
    private byte[] generate_metadata(int amountOfPictures, int dataBits, String name){
        byte[] metadata = new byte[27];

        // Kann sein, dass hier Daten abgeschnitten werden, wenn sie sehr groß sind und sogesehen 1ne Row nur mit Signaturen überdeckt wird.
        // -1 bisschen unsicher, sollte aber dafür sorgen das lastRowDataBist richtig funktioniert (vor allem bei voller Zeile) -> kein Edge case gefunden wo es nicht läuft
        this.rowsOfData = Math.floorDiv((dataBits - 1) / 3, this.width) + 1;
        this.lastRowDataBits = dataBits - (rowsOfData - 1) * this.width * 3 * this.k;
        this.amountOfPictures = amountOfPictures;

        Log.v(TAG, "DataBits with Signature: " + dataBits);
        Log.v(TAG, "rowsOfData: " + rowsOfData);
        Log.v(TAG, "lastRowDataBits: " + lastRowDataBits);
        Log.v(TAG, "width: " + this.width);
        Log.v(TAG, "height: " + this.height);

        int nameBytes = name != null ? name.getBytes().length : 0;
        int dataLength = this.savedDataLength;

        byte[] dataTypeBytes = this.storedDataType.text.getBytes();
        byte[] signatureBytes = this.signature.getBytes();
        byte[] pSafeSignatureBytes = PICTURESAFESIGNATURE.getBytes();
        byte[] compressionTypeBytes = this.compressionType.text.getBytes();

        assert 0 < this.lastRowDataBits;
        assert this.lastRowDataBits <= this.width * 3;
        assert this.rowsOfData <= 65535;
        assert this.lastRowDataBits <= 65535;
        assert this.storedDataType.text.length() == 4;
        assert this.k <= 127;
        assert this.signature != null;
        assert this.signature.length() == 4;
        assert nameBytes <= 255;
        assert amountOfPictures <= 255;
        assert currentPicture <= 255;

        // set metadata in first 22 Bytes
        metadata[0] = pSafeSignatureBytes[0];
        metadata[1] = pSafeSignatureBytes[1];
        metadata[2] = pSafeSignatureBytes[2];
        metadata[3] = pSafeSignatureBytes[3];
        metadata[4] = pSafeSignatureBytes[4];
        metadata[5] = (byte) amountOfPictures;
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
        metadata[22] = (byte) (dataLength >> 24);
        metadata[23] = (byte) (dataLength >> 16);
        metadata[24] = (byte) (dataLength >>  8);
        metadata[25] = (byte) dataLength;
        metadata[26] = (byte) nameBytes;

        return metadata;
    }

    // update Pixel Data with byteData (do the LSB Stuff)
    // overload return rest of byteData (Data not stored within the picture)
    // TODO need another function that sets the amount of pictures used to store data -> not possible when not every picture has been set yet
    public void setData(byte[] byteData, int amountOfPictures, DataTypes dataType, CompressionType compressionType, char[] password, String name) throws NoSuchAlgorithmException {
        // TODO add functions and remove hard coding
        this.storedDataType = dataType;
        this.savedDataLength = byteData.length;
        Log.v(TAG, "Compression Type: " + compressionType.text);
        Log.v(TAG, "password: " + Arrays.toString(password));

        // Compression
        byte[] compressedData = compressionType.compress_data(byteData);

        if(compressedData == null){
            Log.v(TAG, "Compression is null");
        }
        else{
            Log.v(TAG, "CompressedData Length: " + compressedData.length);
        }
        Log.v(TAG, "Data Length: " + byteData.length);

        if(compressedData != null && compressedData.length < byteData.length) {
            byteData = compressedData;
            this.compressionType = compressionType;
        }
        else {
            // Kompressions Array ist größer als nicht komprimiertes Array
            this.compressionType = compressionType.remove_compression();
            Log.v(TAG, "Compression Type: " + this.compressionType.text);
            Log.v(TAG, "COMPRESSION REMOVED");
        }


        // Encryption
        if(this.compressionType.uses_encryption()){
            try {
                byteData = AESEncryption.encrypt(byteData, password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // convert Data to bin
        int[] binData = PictureUtils.bytesToBinary(byteData);
        int[] binSignature = PictureUtils.bytesToBinary(this.signature.getBytes());

        SecureRandom random = SecureRandom.getInstanceStrong();
        int randMax = this.width - (16 + 32 + 1);
        int dataBitIndex = 0;
        int signatureCount = 0;

        for (int row = 1; dataBitIndex < binData.length; row++) {

            int sigPosition = random.nextInt(randMax);

            if(sigPosition >= binData.length - dataBitIndex - 50)
                sigPosition = 0;

            int[] binPos = PictureUtils.int_to_16bit_array(sigPosition);

            int[] sigBits = new int[48];
            System.arraycopy(binPos, 0, sigBits, 0, 16);
            System.arraycopy(binSignature, 0, sigBits, 16, 32);

            int sigStartBit = sigPosition + 16;
            int sigEndBit   = sigStartBit + 31;

            int sigIndex = 0;
            signatureCount += 48;

            for (int col = 0; col < width; col++) {
                // Jeder Pixel der Reihe
                int pixel = pixels[row][col];

                int r = (pixel >> 16) & 1;
                int g = (pixel >> 8) & 1;
                int b = pixel & 1;

                for (int c = 0; c < 3; c++) {
                    // Bit für jeden Pixel
                    int bitPos = col * 3 + c;
                    int bit;

                    if (bitPos < 16 || (bitPos >= sigStartBit && bitPos <= sigEndBit)) {
                        bit = sigBits[sigIndex++];
                    } else {
                        bit = (dataBitIndex < binData.length) ? binData[dataBitIndex++] : 0;
                    }

                    if (c == 0) r = bit;
                    else if (c == 1) g = bit;
                    else b = bit;
                }

                pixels[row][col] = PictureUtils.setLSB(pixel, r, g, b);
            }
        }

        // Metadaten generieren
        byte[] metadata = this.generate_metadata(amountOfPictures, binData.length + signatureCount, name);
        byte[] nameBytes = name.getBytes();
        // TODO check length of name

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
        int bitIndex = 0;
        int pixelsY = 0;

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



    /* ====== Export ====== */
    private Bitmap update_bitmap_pixels() {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] flat = new int[width * height];
        int pos = 0;

        for (int y = 0; y < height; y++) {
            System.arraycopy(pixels[y], 0, flat, pos, width);
            pos += width;
        }

        bmp.setPixels(flat, 0, width, 0, 0, width, height);
        return bmp;
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

    public FileData read_content(int lenInBits, boolean readMetadata, int offsetBits, char[] password) {
        Log.v(TAG, "reading Content...");
        if(!readMetadata)
            Log.v(TAG, "password: " + Arrays.toString(password));
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
        if(!readMetadata){
            binData = PictureUtils.remove_check_signature(binData, this.width, PictureUtils.bytesToBinary(this.signature.getBytes()));
            if(binData == null){
                return null;
            }
        }
        byte[] data = PictureUtils.binaryToBytes(binData);

        if(readMetadata)
            return new FileData(data, this.storedDataType, this.name);

        // Decompression and Decryption
        if (this.compressionType.uses_encryption()) {
            try {
                data = AESEncryption.decrypt(data, password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        data = this.compressionType.decompress_data(data, this.savedDataLength);
        return new FileData(data, this.storedDataType, this.name);
    }
    public FileData read_content(char[] password){
        return this.read_content((this.rowsOfData - 1) * this.width * 3 + this.lastRowDataBits, false, 0, password);
    }
    public FileData read_content(){
        return this.read_content((this.rowsOfData - 1) * this.width * 3 + this.lastRowDataBits, false, 0, null);
    }
    public FileData read_content(int lenInBits, boolean readMetadata){
        return this.read_content(lenInBits, readMetadata, 0, null);
    }


    private Object[] read_metadata(){
        byte[] data = this.read_content(27 * 8, true).content;
        int nameBytes = data[26] & 0xFF;
        String name = new String(this.read_content(nameBytes * 8, true, 27*8, null).content);
        Log.v(TAG, "name Length: " + nameBytes);
        Log.v(TAG, "saved FileName: " + name);
        Log.v(TAG, "data: " + Arrays.toString(data));
        return PictureUtils.convertMetaDataBytes(data, name);
    }
}
