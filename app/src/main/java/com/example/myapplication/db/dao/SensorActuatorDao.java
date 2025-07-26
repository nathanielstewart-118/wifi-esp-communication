package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.entity.SensorActuatorIdWithTitle;

import java.util.List;

@Dao
public interface SensorActuatorDao {

    @Insert
    long insert(SensorActuator sa);

    @Update
    int update(SensorActuator sa);

    @Delete
    int delete(SensorActuator sa);

    @Query("DELETE FROM sensors_and_actuators WHERE title = :title")
    void deleteByTitle(String title);

    @Query("SELECT * FROM sensors_and_actuators")
    LiveData<List<SensorActuator>> getAllSensorsAndActuators();

    @Query("SELECT * FROM sensors_and_actuators WHERE sensor_or_actuator = 0")
    LiveData<List<SensorActuator>> getAllSensors();

    @Query("SELECT * FROM sensors_and_actuators WHERE sensor_or_actuator = 1")
    LiveData<List<SensorActuator>> getAllActuators();


    @Query("SELECT * FROM sensors_and_actuators WHERE id = :id")
    SensorActuator getSensorActuatorById(Long id);

    @Query("SELECT * FROM sensors_and_actuators WHERE title = :title and sensor_or_actuator = :sensorOrActuator")
    List<SensorActuator> getByTitle(String title, int sensorOrActuator);

    @Query("INSERT into sensors_and_actuators('variable_name', 'data_type', 'number_of_channels', 'monitoring', 'real_time_control') values('a', 'uint8', 3, 0, 0)")
    void insertByRaw();

    @Query("SELECT * FROM sensors_and_actuators WHERE id IN (:ids)")
    List<SensorActuator> getByIds(List<Long> ids);

    @Query("SELECT DISTINCT title FROM sensors_and_actuators WHERE sensor_or_actuator = :sensorOrActuator")
    LiveData<List<String>> getAllTitles(int sensorOrActuator);

}
