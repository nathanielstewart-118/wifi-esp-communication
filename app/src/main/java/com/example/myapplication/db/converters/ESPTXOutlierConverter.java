package com.example.myapplication.db.converters;

import androidx.room.TypeConverter;

import com.example.myapplication.db.entity.ESPTXOutlier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ESPTXOutlierConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<ESPTXOutlier> fromJson(String value) {
        Type listType = new TypeToken<List<ESPTXOutlier>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<ESPTXOutlier> list) {
        return gson.toJson(list);
    }
}
