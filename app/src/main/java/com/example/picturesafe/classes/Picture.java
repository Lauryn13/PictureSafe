package com.example.picturesafe.classes;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.enumerators.CompressionType;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeCouldntSavePictureNameInfo;
import com.example.picturesafe.exceptions.PictureSafeDataCorruptedInfo;
import com.example.picturesafe.exceptions.PictureSafeFileNotFoundException;
import com.example.picturesafe.exceptions.PictureSafeIOException;
import com.example.picturesafe.exceptions.PictureSafeMetaDataException;
import com.example.picturesafe.exceptions.PictureSafeOutOfMemory;
import com.example.picturesafe.exceptions.PictureSafePictureTooSmallException;
import com.example.picturesafe.exceptions.PictureSafeSecurityNotSupported;
import com.example.picturesafe.exceptions.PictureSafeWrongPasswordException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

/** Picture
 *  Klasse, die die Bilder als Speichergrundlage verwaltet und somit LSB-Steganographie durchführt.
 *  Kümmert sich um:
 *    - Umwandlung der Datei über die Bitmap zu einem bearbeitbaren Pixel-Array und zurück
 *    - Lesen und Schreiben der versteckten Daten im Bild -> Generierung eines neuen (mit Informationen beinhalteten) Bildes als PNG
 *    - Lesen und Schreiben wichtiger Informationen versteckt als Metadaten im Bild (immer in der ersten Zeile des Bildes)
 *    - automatische Überprüfung ob sich versteckte Daten innerhalb des Bildes befinden
 *    - Signieren des Bildes mit einer ID an einer zufälligen Position innerhalb jeder Zeile, um eventuelle Änderungen zu erkennen und später zusammengehörige Bilder erkennen zu können.
 *          -> eine darausführende überprüfung beim Lesen, ob die gespeicherten Daten zerstört wurden
 */
public class Picture {
    /** Signatur am Anfang der Datei, um eine Bearbeitung durch dieses Programm eindeutig erkennen zu können **/
    private static final String PICTURESAFESIGNATURE = "PSafe";

    /** Bitmap des aktuellen Bildes **/
    public Bitmap bitmap;
    /** Wichtige Informationen des Bildes **/
    private int[][] pixels; // 2D Array zum speichern des Bildes
    public int height;
    public int width;
    /** Anzahl der k-LSB Bits, welche genutzt werden zum Speichern der Daten. Wird aktuell nicht genutzt, da es den schreibe/Lese Algorithmus erheblich verlangsamen würde.
     *  Ein Teil der implementation ist schon vorhanden, aus dem Gründen wird es vorerst drin gelassen und dauerhaft auf 1 gesetzt, um eine spätere vollständige Implementation zu ermöglichen.**/
    public int k;
    /** Maximale Anzahl an Speicherbaren Bytes im Bild **/
    public int storeable_data_in_byte;

    /** Information über den Zusammenhang mit anderen Bildern **/
    public int amountOfPictures; // Gesamtanzahl der genutzten Bilder
    public int currentPicture; // Aktuelle Nummer des Bildes
    public String signature; // Signature des Bildes, ist eher eine Art ID, welche zusammenhängede Bilder identifizier und über die die Vollständigkeit erkannt werden kann. Immer Länge von 4

    /** Information über die Daten, welche im Bild gespeichert wurden **/
    public boolean hasData; // Hat das Bild daten?
    public String name; // Name der gespeicherten Datei
    public boolean dataIsCorrupted; // Sind die Daten auf dem Bild zerstört / Beeinflusst wurden?
    public CompressionType compressionType; // Type der genutzten Kompressions- und Verschlüsselungsmethode
    public DataTypes storedDataType; // Gespeicherter Datentyp
    public int savedDataLength; // Länge in Byte der Ursprünglichen Datei
    private int rowsOfData; // Zeilen in denen Daten gespeichert wurden, inkludiert die letzte (evt. nicht volle Reihe), exkludiert die Zeile der Metadaten
    private int lastRowDataBits; // Anzahl der Bits die in der letzten Spalte gespeichert wurden (nicht Index des Pixels). Wird gebraucht um richtig lesen zu können, da der letzte Datenbit bspw. auch im G-Wert des X. Pixels gespeichert sein könnten.


    /** Konstruktor
     *  Erstellt ein Picture-Objekt aus einer gelieferten Bitmap:
     *   - Überprüft das Bild auf Daten und deren korrektheit.
     *   - Wandelt die Bitmap in ein 2D-Pixel-Array um
     *   - Liest alle wichtigen Metadaten aus dem Bild
     *
     * @param data Bitmap des Bildes
     * @param currentPicture Aktuelle Nummer des Bildes
     * @param signature zu Verwendende Signature des Bildes
     */
    public Picture(Bitmap data, int currentPicture, String signature){
        this.bitmap = data;

        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
        this.k = 1;
        this.currentPicture = currentPicture;

        this.storeable_data_in_byte = ((width * (height-1) * 3 * k) - (32 + 16) * (height - 1)) / 8;
        this.pixels = this.readPixelArray();

        // überprüfung ob Signatur wirklich 4 Stellen hat, sonst kommt es zu Fehlern im Code -> Sollte niemals auftreten, nur zur Sicherheit.
        if(signature.length() != 4)
            throw new PictureSafeBaseException("Signaturfehler", "Die Signatur wurde falsch generiert und ist daher unzulässig");

        // Überprüft die minimal benötigte Bildgröße um Metadaten + zu speichernde Daten beinhalten zu können.
        if(this.width < 72 || this.height < 2)
            throw new PictureSafePictureTooSmallException(this.width, this.height);

        // Überprüfung auf vorhandene Daten
        this.dataIsCorrupted = false;
        this.hasData = this.checkForData(signature);
    }

    /** checkForData
     *  Überprüft das Bild auf vorhandene Daten und liest wichtige Metadaten aus der ersten Zeile.
     *
     * @param signature zu verwendene ID des Bildes
     * @return Hat vorhandene Daten?
     */
    private boolean checkForData(String signature){
        Object[] metadata = this.readMetadata();

        // Wenn die Signatur erkannt wurde, wird versucht die Metadaten auszulesen
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

            return true;
        }
        this.signature = signature;

        return false;
    }


    /** generateMetadata
     *  Generiert folgende Metadaten für ein Bild:
     *    - (1-5 Byte)   PictureSafe Signatur zur Erkennung des Bildes
     *    - (6. Byte)    Anzahl der Gesamtgenutzten Bilder für die gespeicherte Datei
     *    - (7. Byte)    Aktuelle Nummer des Bildes innerhalb der Datei (Reihenfolge)
     *    - (8-9 Byte)   Anzahl der Zeilen, welche Daten enthalten
     *    - (10-11 Byte) Anzahl der Bits die in der letzten Spalte gespeichert wurden
     *    - (12-15 Byte) Datentyp der gespeicherten Datei
     *    - (16. Byte)   Anzahl der LSB Bits, welche genutzt werden zum Speichern der Daten (k)
     *    - (17-20 Byte) Signature/ID des Bildes
     *    - (21-22 Byte) Kompressions- und Verschlüsselungsmethode
     *    - (23-26 Byte) Länge der Daten, welche im Bild gespeichert wurden
     *    - (27. Byte)   Bytelänge des Dateinamens der gespeicherten Datei
     *    - (28 - Bytelänge des Namens) Name der gespeicherten Datei (wird nur gespeichert, wenn genug Speicherplatz, also Breite des Bildes) vorhanden ist.
     *
     * @param amountOfPictures Anzahl der Bilder, welche genutzt wurden um die ursprüngliche Datei zu speichern
     * @param dataBits Anzahl der Bits, die im Bild gespeichert wurden (Inklusive Signaturen und Verschlüsselungs overhead, exklusiv der Metadaten)
     * @param name Name der zu speichernden Datei
     * @return Byte-Array der Metadaten
     */
    private byte[] generateMetadata(int amountOfPictures, int dataBits, String name){
        byte[] metadata = new byte[27];

        // Berechnung der Zeilen, die Daten enthalten & der Bits, die in der letzten Zeile gespeichert wurden, um später die tatsächlichen Daten erkennen zu können
        this.rowsOfData = Math.floorDiv((dataBits - 1) / 3, this.width) + 1;
        this.lastRowDataBits = dataBits - (rowsOfData - 1) * this.width * 3 * this.k;
        this.amountOfPictures = amountOfPictures;
        this.name = name;

        int nameBytes = name != null ? name.getBytes().length : 0;
        int dataLength = this.savedDataLength;

        // Umwandlung der Strings in Bytes
        byte[] dataTypeBytes = this.storedDataType.text.getBytes();
        byte[] signatureBytes = this.signature.getBytes();
        byte[] pSafeSignatureBytes = PICTURESAFESIGNATURE.getBytes();
        byte[] compressionTypeBytes = this.compressionType.text.getBytes();

        // Überprüfen, dass die maximale Bildgröße eingehalten wurde (Sonst reichen die freien Bytes in den Metadaten nicht aus um z.b. LastRowDataBits speichern zu können)
        if(this.rowsOfData > 65535 || this.lastRowDataBits > 65535)
            throw new PictureSafeMetaDataException("Das ausgewählte Bild ist zu groß, sodass es nicht verarbeitet werden kann.\nMaximale Größe: 21844x65534 Pixel\nAusgewählte Größe: " + this.width + "x" + this.height + " Pixel", false);

        // Überprüfen, ob der Name zu lang ist um ihn noch im Bild speichern zu können
        if(this.width * 3 - 1 < 216 + nameBytes * 8){
            this.name = null;
            nameBytes = 0;
        }

        // Daten sind verschlüsselt -> Name wird nicht gespeichert
        if(this.compressionType.usesEncryption()){
            nameBytes = 0;
            this.name = null;
        }

        // Maximale Anzahl von Bildern die Zusammenhängend Daten speichern zu können (Sonst reichen MetadatenBytes nicht aus um die Zahl zu speichern)
        if(amountOfPictures > 256 || this.currentPicture > 256)
            throw new PictureSafeMetaDataException("Es können nicht mehr als 256 Bilder ausgewählt werden.", false);

        // Maximale Anzahl von Bytes des Namens (Sonst reichen Metadatenbytes nicht aus um die Anzahl richtig speichern zu können -> falscher keiner oder zu langer Name wird gelesen)
        if(nameBytes > 256)
            throw new PictureSafeMetaDataException("Name ist zu lang, sodass er nicht gespeichert werden kann.", true);

        // Setzen der Metadaten innerhalb der ersten 27-Bytes der ersten Zeile -> Hier Array generiert, da es mit dem Namen (sollte er gespeichert werden) nachher Verknüpft und dann geschrieben wird
        metadata[0] = pSafeSignatureBytes[0];
        metadata[1] = pSafeSignatureBytes[1];
        metadata[2] = pSafeSignatureBytes[2];
        metadata[3] = pSafeSignatureBytes[3];
        metadata[4] = pSafeSignatureBytes[4];
        metadata[5] = (byte) amountOfPictures;
        metadata[6] = (byte) this.currentPicture;
        metadata[7] = (byte) (this.rowsOfData >> 8);
        metadata[8] = (byte) this.rowsOfData;
        metadata[9] = (byte) (this.lastRowDataBits >> 8);
        metadata[10] = (byte) this.lastRowDataBits;
        metadata[11] = dataTypeBytes[0];
        metadata[12] = dataTypeBytes[1];
        metadata[13] = dataTypeBytes[2];
        metadata[14] = dataTypeBytes[3];
        metadata[15] = (byte) this.k;
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

    /** setData
     *  Speichert Daten in einem Bild:
     *   - Berechnet neue Pixelwerte mit den vorhandenen Bildern
     *   - Speichert die Metadaten im Bild
     *   - Komprimiert und verschlüsselt die Daten
     *   - generiert Signaturen im Bild und speichert diese an zufälligen Positionen
     *
     * @param mvm MainViewModel, um nicht-kritische Fehler vorübergehend zu speichern und später zu werfen
     * @param byteData Byte-Array der zu speichernden Datei
     * @param originalByteLength Unkomprimierte Länge der zu speichernden Datei
     * @param amountOfPictures Anzahl der Bilder, indem die Datei gespeichert wird
     * @param dataType Datentyp der zu speichernden Datei
     * @param compressionType Kompressions- und Verschlüsselungsmethode
     * @param password Passwort, um die Daten zu verschlüsseln
     * @param name Name der zu speichernden Datei
     * @throws IOException Fehler beim Lesen/Schreiben der Datei
     */
    public void setData(MainViewModel mvm, byte[] byteData, int originalByteLength, int amountOfPictures, DataTypes dataType, CompressionType compressionType, char[] password, String name) throws IOException{
        this.storedDataType = dataType;
        this.savedDataLength = originalByteLength;
        this.compressionType = compressionType;

        // Verschlüsselung der Daten
        if(this.compressionType.usesEncryption()){
            try {
                byteData = AESEncryption.encrypt(byteData, password);
            } catch (NoSuchAlgorithmException e) {
                throw new PictureSafeSecurityNotSupported();
            } catch (GeneralSecurityException e){
                throw new PictureSafeWrongPasswordException();
            } catch (IOException e){
                throw new PictureSafeIOException(e);
            }
        }

        // Byte-Daten in Binärd-Daten umwandeln
        int[] binData = PictureUtils.bytesToBinary(byteData);
        int[] binSignature = PictureUtils.bytesToBinary(this.signature.getBytes());

        // möglichst zuverlässigter Zufallszahlengenerator für die Positionen der Signatur
        SecureRandom random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e){
            throw new PictureSafeSecurityNotSupported();
        }
        Objects.requireNonNull(random);

        int randMax = this.width - (16 + 32 + 1);
        int dataBitIndex = 0;
        int signatureCount = 0;

        // Generieren der Signatur Daten für jede Zeile im Bild
        for (int row = 1; dataBitIndex < binData.length; row++) {
            // Signaturposition in der aktuellen Zeile generieren
            int sigPosition = random.nextInt(randMax);

            // Special Case: Daten Enden in der Zeile -> SignaturPosition muss vor dem Ende der Daten stehen
            if(sigPosition >= binData.length - dataBitIndex - 50)
                sigPosition = 0;

            int[] binPos = PictureUtils.intTo16BitArray(sigPosition);
            int[] sigBits = new int[48];
            // Zusammensetzen der Position der Signatur (ersten 16bit und eigentlicher Signatur 32bit, um später schreiben zu können)
            System.arraycopy(binPos, 0, sigBits, 0, 16);
            System.arraycopy(binSignature, 0, sigBits, 16, 32);

            // Bitposition des ersten Bits der Signatur in der aktuellen Zeile
            int sigStartBit = sigPosition + 16;
            // Bitposition des letzten Bits der Signatur in der aktuellen Zeile
            int sigEndBit   = sigStartBit + 31;
            // Index von sigBits, welcher als nächstes Geschrieben werden soll
            int sigIndex = 0;
            // Insgesamte Bits, welche die Signatur verbraucht (zusätzlich zu den eigentlichen Datenbits)
            signatureCount += 48;

            // Bits für jeden Pixel in der Zeile generieren
            for (int col = 0; col < this.width; col++) {
                // Jeder Pixel der Reihe lesen
                int pixel = this.pixels[row][col];

                int r = (pixel >> 16) & 1;
                int g = (pixel >> 8) & 1;
                int b = pixel & 1;

                // R,G,B Werte des Pixels neu berechnen
                for (int c = 0; c < 3; c++) {
                    // Bit für jeden Pixel
                    int bitPos = col * 3 + c;
                    int bit;

                    // Index im Bild ist entweder am Anfang (erste 16bit und damit die Position der Signatur) oder innerhalb der Signaturbits (Signaturbits werden dort gespeichert)
                    if (bitPos < 16 || (bitPos >= sigStartBit && bitPos <= sigEndBit))
                        bit = sigBits[sigIndex++];
                    else
                        // Eigentliche Datenbits werden geschrieben
                        bit = (dataBitIndex < binData.length) ? binData[dataBitIndex++] : 0;

                    if (c == 0)
                        r = bit;
                    else if (c == 1)
                        g = bit;
                    else
                        b = bit;
                }

                // speichern des neu generierten Pixels
                this.pixels[row][col] = PictureUtils.setLSB(pixel, r, g, b);
            }
        }

        // Metadaten generieren
        byte[] metadata = this.generateMetadata(amountOfPictures, binData.length + signatureCount, name);
        byte[] nameBytes = (this.name != null) ? this.name.getBytes() : null;

        // Infotext, dass der Name nicht gespeichert werden kann NACHDEM die Daten geschrieben wurden anzeigen
        if(nameBytes == null && !this.compressionType.usesEncryption())
            mvm.waitingException = new PictureSafeCouldntSavePictureNameInfo();

        // Generierung der Metadaten und Namensdaten im Bild
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(metadata);
        if(nameBytes != null)
            out.write(nameBytes);

        int[] metaBin = PictureUtils.bytesToBinary(out.toByteArray());
        int bitIndex = 0;
        int pixelsY = 0;

        // Metadaten schreiben
        while (bitIndex < metaBin.length) {
            int bitR = metaBin[bitIndex++];
            int bitG = (bitIndex < metaBin.length) ? metaBin[bitIndex++] : 0;
            int bitB = (bitIndex < metaBin.length) ? metaBin[bitIndex++] : 0;

            this.pixels[0][pixelsY] = PictureUtils.setLSB(this.pixels[0][pixelsY], bitR, bitG, bitB);

            pixelsY++;
        }

        // Neue Bitmap für das Bild generieren
        this.bitmap = updateBitmapPixels();
    }

    /** updateBitmapPixels
     *  Generiert eine neue Bitmap aus den Pixel-Werten
     *
     * @return neue Bitmap
     */
    private Bitmap updateBitmapPixels() {
        Bitmap bmp = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);

        // 1D Array, indem die Daten der Pixel FLACH neu geschrieben werden
        int[] flat = new int[this.width * this.height];
        int pos = 0;

        // Setzen der Daten im Array
        for (int y = 0; y < this.height; y++) {
            System.arraycopy(this.pixels[y], 0, flat, pos, this.width);
            pos += this.width;
        }

        // Setzen des flachen Arrays in die Bitmap -> deutlich effizienter als das 2D Array
        bmp.setPixels(flat, 0, this.width, 0, 0, this.width, this.height);
        return bmp;
    }

    /** generatePng
     *  Generiert eine PNG-Datei aus dem Bild
     *
     * @param context Context der aktuellen Acitivty
     * @throws IOException Fehler beim Lesen/Schreiben der Datei
     */
    public void generatePng(Context context) throws IOException {
        // TYP, Name und speicherort festlegen
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "output.png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/PictureSafe");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Objects.requireNonNull(uri);

        // Outputstream zum schreiben der Datei
        OutputStream out;
        try {
            out = context.getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            throw new PictureSafeFileNotFoundException();
        }
        Objects.requireNonNull(out);

        // erstellen der neuen Datei
        this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
    }

    /** readPixelArray
     *  Generiert ein 2D-Integer-Array aus den Pixeln der Bitmap
     *
     * @return 2D-Integer-Array
     */
    private int[][] readPixelArray(){
        int[][] pixels = new int[this.height][this.width];

        int[] flat;
        try {
            flat = new int[this.width * this.height];
        } catch (OutOfMemoryError e){
            throw new PictureSafeOutOfMemory();
        }
        Objects.requireNonNull(flat);

        // lesen der Pixel in ein 1D Array -> extrem effizient (Vergleichsweise zum lesen in 2D)
        this.bitmap.getPixels(flat, 0, this.width, 0, 0, this.width, this.height);

        // umwandeln der Pixel in ein 2D Array
        for(int y = 0, i = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++, i++) {
                pixels[y][x] = flat[i];
            }
        }
        return pixels;
    }

    /** readContent
     *  Liest Daten aus dem Bild:
     *  kann genutzt werden um Metadaten zu lesen oder richtige Daten:
     *   - Liest Bitdaten mit der Länge lenInBits (einschließlich Signaturebits) aus den Daten
     *   - Überprüft vollständigkeit der Signatur und schmeißt im Fall von Korrupten Daten einen Fehler
     *   - Entschlüsselt und Dekomprimiert Daten (insofern es keine Metadaten sind)
     *
     * @param lenInBits Länge der zu lesenden Daten in Bits
     * @param readMetadata Sind die Daten Metadaten
     * @param offsetBits Offset an Bits, die übersprungen werden (wird zum Lesen des Namens des ursprünglichen Bildes gebraucht)
     * @param password Passwort der verschlüsselten Daten
     * @return Daten als FileData-Objekt
     */
    public FileData readContent(int lenInBits, boolean readMetadata, int offsetBits, char[] password) {
        byte[] binData = new byte[lenInBits];
        int bitOffset = offsetBits % 3;
        int startPixel = offsetBits / 3;
        int extraRow = readMetadata ? 0 : 1;
        int pixelsX = startPixel / this.width + extraRow;
        int pixelsY = startPixel % this.width;
        int bi = 0;

        while (bi < lenInBits) {
            int pixel = this.pixels[pixelsX][pixelsY];

            // RGB-Werte des Pixels aus dem Integer-Pixel generieren
            int[] bits = {
                    (pixel >> 16) & 1,
                    (pixel >> 8)  & 1,
                    pixel & 1
            };

            // Offset Pixel überspringen und Bits auslesen
            for (int c = bitOffset; c < 3 && bi < lenInBits; c++)
                binData[bi++] = (byte) bits[c];

            bitOffset = 0; // nur beim ersten Pixel relevant

            if (++pixelsY == this.width) {
                pixelsY = 0;
                pixelsX++;
            }
        }

        // Überprüfen der Metadaten
        if(!readMetadata){
            try {
                binData = PictureUtils.removeCheckSignature(binData, this.width, PictureUtils.bytesToBinary(this.signature.getBytes()));
            } catch (PictureSafeDataCorruptedInfo e) {
                // Korrupte Daten
                this.dataIsCorrupted = true;
                throw new PictureSafeDataCorruptedInfo();
            }
        }
        // Umwandlung Binär-Daten zu Byte-Daten
        byte[] data = PictureUtils.binaryToBytes(binData);

        if(readMetadata)
            return new FileData(data, this.storedDataType, this.name, data.length);

        // Entschlüsseln
        if (this.compressionType.usesEncryption()) {
            try {
                data = AESEncryption.decrypt(data, password);
            } catch (NoSuchAlgorithmException e) {
                throw new PictureSafeSecurityNotSupported();
            } catch (GeneralSecurityException e) {
                throw new PictureSafeWrongPasswordException();
            }
        }

        // Dekomprimieren der Daten
        data = this.compressionType.decompressData(data, this.savedDataLength);
        // erstellen einer FileData, die zurückgegeben wird
        return new FileData(data, this.storedDataType, this.name, this.savedDataLength);
    }
    /** readContent-Overload
     *  Liest Daten aus dem Bild, angepasst an die reinen Bindaten des Files (keine Metadaten)
     *  generiert die Anzahl der Datenbits aus dem Bild anhand der gelesenen Metadaten
     *
     * @param password Passwort der verschlüsselten Daten
     * @return Daten als FileData-Objekt
     */
    public FileData readContent(char[] password){
        return this.readContent((this.rowsOfData - 1) * this.width * 3 + this.lastRowDataBits, false, 0, password);
    }
    /** readContent-Overload
     *  Liest Daten aus dem Bild, vor allem Metadaten
     *
     * @param lenInBits Länge der zu lesenden Daten in Bits
     * @param readMetadata Sind die Daten Metadaten
     * @return Daten als FileData-Objekt
     */
    public FileData readContent(int lenInBits, boolean readMetadata){
        return this.readContent(lenInBits, readMetadata, 0, null);
    }

    /** readMetadata
     *  Lesen der Metadaten aus der ersten Zeile
     *  - Lesen der Daten und umwandlung in die entsprechenden Datenformate
     *  - Lesen des Namens anhand der NameBytes aus den Metadaten
     *
     * @return Liste mit den Metadaten als Objekte
     */
    private Object[] readMetadata(){
        // Lesen der ByteDaten
        byte[] data = this.readContent(27 * 8, true).content;
        // Umwandlung der nameBytes um Namen lesen zu können
        int nameBytes = data[26] & 0xFF;
        String name = null;
        if (nameBytes != 0 && new String(data, 0, 5).equals(PICTURESAFESIGNATURE))
            name = new String(this.readContent(nameBytes * 8, true, 27*8, null).content);
        // Erstellen des Arrays mit den Metadaten
        return PictureUtils.convertMetaDataBytes(data, name);
    }
}
