package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.ESPRXRT;
import com.example.myapplication.db.entity.ESPTX;

import java.util.List;

@Dao
public interface ESPRXRTDao {

    @Insert
    long insert(ESPRXRT esprxrt);

    @Update
    int update(ESPRXRT esprxrt);

    @Delete
    int delete(ESPRXRT esprxrt);

    @Query("SELECT * FROM esp_tx")
    LiveData<List<ESPRXRT>> getAllESPRXRTs();

    @Query("SELECT * FROM esp_rxrt WHERE sensor_or_actuator = 1")
    LiveData<List<ESPRXRT>> getESPRXRTSensors();

    @Query("SELECT * FROM esp_rxrt WHERE sensor_or_actuator = 2")
    LiveData<List<ESPRXRT>> getESPRXRTActuators();



    @Query("SELECT * FROM esp_rxrt WHERE id = :id")
    ESPRXRT getESPRXRXById(int id);

}
