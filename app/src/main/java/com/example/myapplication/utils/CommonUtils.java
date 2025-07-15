package com.example.myapplication.utils;

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
}
