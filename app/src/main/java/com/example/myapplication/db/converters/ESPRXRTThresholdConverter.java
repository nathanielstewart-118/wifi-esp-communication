package com.example.myapplication.db.converters;

import androidx.room.TypeConverter;

import com.example.myapplication.db.entity.ESPRXRTThreshold;
import com.example.myapplication.db.entity.ESPTXOutlier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ESPRXRTThresholdConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<ESPRXRTThreshold> fromJson(String value) {
        Type listType = new TypeToken<List<ESPRXRTThreshold>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<ESPRXRTThreshold> list) {
        return gson.toJson(list);
    }
}
