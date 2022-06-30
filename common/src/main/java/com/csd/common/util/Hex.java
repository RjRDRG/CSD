package com.csd.common.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Hex {
    private static final HexEncoder encoder = new HexEncoder();

    public Hex() {
    }

    public static String toHexString(byte[] var0) {
        return toHexString(var0, 0, var0.length);
    }

    public static String toHexString(byte[] var0, int var1, int var2) {
        byte[] var3 = encode(var0, var1, var2);
        return  new String(asCharArray(var3));
    }

    public static char[] asCharArray(byte[] var0) {
        char[] var1 = new char[var0.length];

        for(int var2 = 0; var2 != var1.length; ++var2) {
            var1[var2] = (char)(var0[var2] & 255);
        }

        return var1;
    }

    public static byte[] encode(byte[] var0) {
        return encode(var0, 0, var0.length);
    }

    public static byte[] encode(byte[] var0, int var1, int var2) {
        ByteArrayOutputStream var3 = new ByteArrayOutputStream();

        try {
            encoder.encode(var0, var1, var2, var3);
        } catch (Exception var5) {
            throw new RuntimeException("exception encoding Hex string: " + var5.getMessage(), var5);
        }

        return var3.toByteArray();
    }

    public static int encode(byte[] var0, OutputStream var1) throws IOException {
        return encoder.encode(var0, 0, var0.length, var1);
    }

    public static int encode(byte[] var0, int var1, int var2, OutputStream var3) throws IOException {
        return encoder.encode(var0, var1, var2, var3);
    }

    public static byte[] decode(byte[] var0) {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();

        try {
            encoder.decode(var0, 0, var0.length, var1);
        } catch (Exception var3) {
            throw new RuntimeException("exception decoding Hex data: " + var3.getMessage(), var3);
        }

        return var1.toByteArray();
    }

    public static byte[] decode(String var0) {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();

        try {
            encoder.decode(var0, var1);
        } catch (Exception var3) {
            throw new RuntimeException("exception decoding Hex string: " + var3.getMessage(), var3);
        }

        return var1.toByteArray();
    }

    public static int decode(String var0, OutputStream var1) throws IOException {
        return encoder.decode(var0, var1);
    }
}
