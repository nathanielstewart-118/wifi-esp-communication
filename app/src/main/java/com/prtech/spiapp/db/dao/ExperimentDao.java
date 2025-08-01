package com.prtech.spiapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prtech.spiapp.db.entity.Experiment;

import java.util.List;

@Dao
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

    @Query("SELECT * FROM experiments WHERE title = :title")
    List<Experiment> getExperimentsByTitle(String title);

    @Query("SELECT DISTINCT title FROM experiments")
    LiveData<List<String>> getAllTitles();

    @Query("DELETE FROM experiments WHERE title=:title")
    void deleteByTitle(String title);

    @Query("SELECT * FROM experiments WHERE title=:title")
    List<Experiment> getByTitle(String title);

}
