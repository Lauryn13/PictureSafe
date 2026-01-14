package com.example.picturesafe.classes;

import com.example.picturesafe.exceptions.PictureSafeDataCorruptedInfo;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

/**
 * Compression
 *
 * Die Klasse kümmert sich um die Komprimierung und Dekomprimierung von Byte-Arrays im Projekt.
 */
public class Compression {

    /** compressLZ4
     * Komprimiert ein Byte-Array mit dem LZ4-Algorithmus.
     * Der schnellste genutzte Algorithmus, dafür aber auch die geringste Kompressionsdichte.
     *
     * @param data zu komprimierendes Byte-Array
     * @return komprimiertes Byte-Array
     */
    public static byte[] compressLZ4(byte[] data){
        LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

        byte[] out = new byte[compressor.maxCompressedLength(data.length)];
        // Länge wird zum wiederherstellen der Daten benötigt
        int len = compressor.compress(data, 0, data.length, out, 0);

        return Arrays.copyOf(out, len);
    }

    /** decompressLZ4
     * Dekomprimiert ein Byte-Array mit dem LZ4-Algorithmus.
     *
     * @param data zu dekomprimierendes Byte-Array
     * @param originalSize wird benötigt um die Originalen Daten wiederherzustellen. Wird daher in den Metadaten des Bildes mit gespeichert.
     * @return dekomprimiertes Byte-Array
     */
    public static byte[] decompressLZ4(byte[] data, int originalSize){
        LZ4SafeDecompressor decompressor = LZ4Factory.fastestInstance().safeDecompressor();

        byte[] restored = new byte[originalSize];
        decompressor.decompress(data, 0, data.length, restored, 0);

        return restored;
    }

    /** compressDeflate
     * Komprimiert ein Byte-Array mit dem Deflate-Algorithmus.
     * Einstellbar in Schnelligkeit und Kompressionsdichte, wird daher für den Mittleren und Starken Algorithmus genutzt.
     *
     * Deflate & Inflate angelehnt durch folgende Implementierung: https://ssojet.com/compression/compress-files-with-deflate-in-java#understanding-deflate-compression-in-java
     *
     * @param data zu komprimierendes Byte-Array
     * @param level Kompressionsstufe
     * @return komprimiertes Byte-Array
     * @throws IOException Bei Fehlern beim Lesen/Schreiben
     */
    public static byte[] compressDeflate(byte[] data, int level) throws IOException {
        Deflater deflater = new Deflater(level, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);

        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        }

        return baos.toByteArray();
    }

    /** decompressDeflate
     * Dekomprimiert ein Byte-Array mit dem Deflate-Algorithmus.
     *
     * Deflate & Inflate angelehnt durch folgende Implementierung: https://ssojet.com/compression/compress-files-with-deflate-in-java#understanding-deflate-compression-in-java
     *
     * @param data zu dekomprimierendes Byte-Array
     * @return dekomprimiertes Byte-Array
     */
    public static byte[] decompressDeflate(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        try {
            while (!inflater.finished()) {
                // Dekromprimiert in einzelnen Blöcken
                int count = inflater.inflate(buffer);
                baos.write(buffer, 0, count);
            }
        } catch(DataFormatException e){
            throw new PictureSafeDataCorruptedInfo();
        }finally {
            inflater.end();
        }

        return baos.toByteArray();
    }
}
