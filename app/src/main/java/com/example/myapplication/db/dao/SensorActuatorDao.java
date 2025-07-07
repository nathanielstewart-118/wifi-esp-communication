package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.SensorActuator;

import java.util.List;

@Dao
public interface SensorActuatorDao {

    @Insert
    long insert(SensorActuator sa);

    @Update
    int update(SensorActuator sa);

    @Delete
    int delete(SensorActuator sa);

    @Query("SELECT * FROM sensors_and_actuators")
    LiveData<List<SensorActuator>> getAllSensorsAndActuators();

    @Query("SELECT * FROM sensors_and_actuators WHERE id = :id")
    LiveData<SensorActuator> getSensorActuatorById(int id);

    @Query("INSERT into sensors_and_actuators('variable_name', 'data_type', 'number_of_channels', 'monitoring', 'real_time_control') values('a', 'uint8', 3, 0, 0)")
    void insertByRaw();

}
