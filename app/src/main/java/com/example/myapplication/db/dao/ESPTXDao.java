package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.ESPTX;

import java.util.List;

@Dao
public interface ESPTXDao {

    @Insert
    long insert(ESPTX esptx);

    @Update
    int update(ESPTX esptx);

    @Delete
    int delete(ESPTX esptx);

    @Query("SELECT * FROM esp_tx")
    LiveData<List<ESPTX>> getAllESPTXes();

    @Query("SELECT * FROM esp_tx WHERE id = :id")
    ESPTX getESPTXById(int id);

    @Query("UPDATE esp_tx SET deleted = 1 WHERE id = :recordId")
    int softDeleteById(Long recordId);
}
