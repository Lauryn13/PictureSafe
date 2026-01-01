package com.example.picturesafe.classes;

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

public class Compression {
    private Compression(){}

    public static byte[] compressLZ4(byte[] data){
        LZ4Factory f = LZ4Factory.fastestInstance();
        LZ4Compressor c = f.fastCompressor();

        byte[] out = new byte[c.maxCompressedLength(data.length)];
        int len = c.compress(data, 0, data.length, out, 0);

        return Arrays.copyOf(out, len);
    }

    public static byte[] decompressLZ4(byte[] data, int originalSize){
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4SafeDecompressor decompressor = factory.safeDecompressor();

        byte[] restored = new byte[originalSize];
        decompressor.decompress(data, 0, data.length, restored, 0);

        return restored;
    }

    public static byte[] compressDeflate(byte[] data, int level){
        Deflater deflater = new Deflater(level, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

        try (DeflaterOutputStream dos = new DeflaterOutputStream(bos, deflater)) {
            dos.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bos.toByteArray();
    }

    public static byte[] decompressDeflate(byte[] data){
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) break;
                bos.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new RuntimeException("Deflate decompression failed", e);
        } finally {
            inflater.end();
        }

        return bos.toByteArray();
    }
}
