package com.prtech.spiapp.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prtech.spiapp.db.entity.Monitoring;

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

    @Query("SELECT * FROM monitorings WHERE created_at > :afterTimestamp ORDER BY created_at ASC LIMIT :limit")
    List<Monitoring> getRecordsAfter(long afterTimestamp, int limit);

    @Query("SELECT MIN(created_at) FROM monitorings")
    long getFirstTimestamp();

    @Query("SELECT count(*) as count FROM monitorings")
    Long getRecordsCount();

    @Query("SELECT * FROM monitorings LIMIT :size OFFSET :offset")
    List<Monitoring> getMonitoringsByOffset(Long offset, Integer size);


}
