package com.prtech.spiapp.db.converters;

import androidx.room.TypeConverter;

import com.prtech.spiapp.db.entity.VisualizationRange;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class VisualizationRangeConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<VisualizationRange> fromJson(String value) {
        Type listType = new TypeToken<List<VisualizationRange>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<VisualizationRange> list) {
        return gson.toJson(list);
    }

}
