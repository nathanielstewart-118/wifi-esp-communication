package com.example.myapplication.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.Monitoring;

import java.util.List;

@Dao
public interface MonitoringDao {
    @Insert
    long insert(Monitoring monitoring);

    @Update
    int update(Monitoring monitoring);

    @Delete
    int delete(Monitoring monitoring);

    @Query("SELECT * FROM monitorings")
    List<Monitoring> getAllMonitorings();

    @Query("SELECT * FROM monitorings WHERE id = :id")
    Monitoring getMonitoringById(Long id);

}
