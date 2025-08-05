package com.prtech.spiapp.utils;

import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    public static Integer string2Int(String input, Integer defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Float string2Float(String input, Float defaultValue) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void selectSpinnerItemWithContent(Spinner spinner, String targetValue) {
        if (spinner == null || targetValue == null) return;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            if (item.equals(targetValue)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public static String long2DateTimeString(Long milliSeconds) {

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy.MM.dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        String formattedDate = formatter.format(Instant.ofEpochMilli(milliSeconds));
        return formattedDate;
    }
}
