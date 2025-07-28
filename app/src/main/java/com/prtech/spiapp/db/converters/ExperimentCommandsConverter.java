package com.prtech.spiapp.db.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.lang.reflect.Type;

public class ExperimentCommandsConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<String> fromJson(String value) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<String> list) {
        return gson.toJson(list);
    }
}
