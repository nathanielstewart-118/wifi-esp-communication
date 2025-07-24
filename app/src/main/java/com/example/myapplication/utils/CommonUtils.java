package com.example.myapplication.utils;

import java.nio.ByteBuffer;
import java.util.Random;

public class CommonUtils {

    public static int getNumberOfBytesFromDataTypeString(String type) {
        switch(type) {
            case "float":
                return 4;
            case "double":
                return 8;
            case "uint8":
            case "int8":
                return 1;
            case "uint16":
            case "int16":
                return 2;
            case "uint24":
            case "int24":
                return 3;
            default:
                return 0;
        }
    }

    public static float generateRandomValueInInterval(Float a, Float b) {
        Random random = new Random();
        return a + random.nextFloat() * (b - a);
    }

    public static byte[] fromStringToByteArray(String input) {
        input = input.replaceAll("\\[|\\]|\\s", ""); // Remove [ ] and spaces
        String[] byteValues = input.split(",");
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0; i < byteValues.length; i++) {
            bytes[i] = Byte.parseByte(byteValues[i]);
        }
        return bytes;
    }
}
