package com.prtech.spiapp.db.converters;

import androidx.room.TypeConverter;

import com.prtech.spiapp.db.entity.ESPOutlier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ESPTXOutlierConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<ESPOutlier> fromJson(String value) {
        Type listType = new TypeToken<List<ESPOutlier>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<ESPOutlier> list) {
        return gson.toJson(list);
    }
}
