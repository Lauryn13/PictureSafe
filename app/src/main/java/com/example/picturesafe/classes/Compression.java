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

public class Compression {
    private Compression(){}

    public static byte[] compressLZ4(byte[] data){
        LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

        byte[] out = new byte[compressor.maxCompressedLength(data.length)];
        int len = compressor.compress(data, 0, data.length, out, 0);

        return Arrays.copyOf(out, len);
    }

    public static byte[] decompressLZ4(byte[] data, int originalSize){
        LZ4SafeDecompressor decompressor = LZ4Factory.fastestInstance().safeDecompressor();

        byte[] restored = new byte[originalSize];
        decompressor.decompress(data, 0, data.length, restored, 0);

        return restored;
    }

    public static byte[] compressDeflate(byte[] data, int level) throws IOException {
        Deflater deflater = new Deflater(level, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);

        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        }

        return baos.toByteArray();
    }

    public static byte[] decompressDeflate(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) break;
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
