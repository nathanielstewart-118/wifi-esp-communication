package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.Visualization;

import java.util.List;

public interface VisualizationDao {

    @Insert
    long insert(Visualization visualization);

    @Update
    int update(Visualization visualization);

    @Delete
    int delete(Visualization visualization);

    @Query("SELECT * FROM visualizations")
    LiveData<List<Visualization>> getAllVisualizations();

    @Query("SELECT * FROM visualizations WHERE id = :id")
    Visualization getVisualizationById(int id);
}
