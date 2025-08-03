package com.prtech.spiapp.db.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class MapConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromMap(Map<Long, Long> map) {
        return gson.toJson(map);
    }

    @TypeConverter
    public static Map<Long, Long> toMap(String json) {
        Type type = new TypeToken<Map<Long, Long>>() {}.getType();
        return gson.fromJson(json, type);
    }
}

