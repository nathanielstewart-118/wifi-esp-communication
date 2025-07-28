package com.prtech.spiapp.db.converters;

import androidx.room.TypeConverter;

import com.prtech.spiapp.db.entity.CommandThreshold;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CommandThresholdConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<CommandThreshold> fromJson(String value) {
        Type listType = new TypeToken<List<CommandThreshold>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<CommandThreshold> list) {
        return gson.toJson(list);
    }
}
