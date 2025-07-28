package com.prtech.spiapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prtech.spiapp.db.entity.ESPPacket;

import java.util.List;

@Dao
public interface SensorActuatorDao {

    @Insert
    long insert(ESPPacket sa);

    @Update
    int update(ESPPacket sa);

    @Delete
    int delete(ESPPacket sa);

    @Query("DELETE FROM esp_packets WHERE title = :title")
    void deleteByTitle(String title);

    @Query("SELECT * FROM esp_packets")
    LiveData<List<ESPPacket>> getAllSensorsAndActuators();

    @Query("SELECT * FROM esp_packets WHERE sensor_or_actuator = 0")
    LiveData<List<ESPPacket>> getAllSensors();

    @Query("SELECT * FROM esp_packets WHERE sensor_or_actuator = 1")
    LiveData<List<ESPPacket>> getAllActuators();


    @Query("SELECT * FROM esp_packets WHERE id = :id")
    ESPPacket getSensorActuatorById(Long id);

    @Query("SELECT * FROM esp_packets WHERE title = :title and sensor_or_actuator = :sensorOrActuator")
    List<ESPPacket> getByTitle(String title, int sensorOrActuator);

    @Query("INSERT into esp_packets('variable_name', 'data_type', 'number_of_channels', 'monitoring', 'real_time_control') values('a', 'uint8', 3, 0, 0)")
    void insertByRaw();

    @Query("SELECT * FROM esp_packets WHERE id IN (:ids)")
    List<ESPPacket> getByIds(List<Long> ids);

    @Query("SELECT DISTINCT title FROM esp_packets WHERE sensor_or_actuator = :sensorOrActuator")
    LiveData<List<String>> getAllTitles(int sensorOrActuator);

}
