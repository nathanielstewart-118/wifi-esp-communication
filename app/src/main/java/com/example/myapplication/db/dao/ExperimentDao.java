package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.Experiment;

import java.util.List;

public interface ExperimentDao {
    @Insert
    long insert(Experiment experiment);

    @Update
    int update(Experiment experiment);

    @Delete
    int delete(Experiment experiment);

    @Query("SELECT * FROM experiments")
    LiveData<List<Experiment>> getAllExperiments();

    @Query("SELECT * FROM experiments WHERE id = :id")
    Experiment getExperimentById(int id);
}
