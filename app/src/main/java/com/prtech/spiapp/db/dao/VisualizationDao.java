package com.prtech.spiapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.prtech.spiapp.db.entity.Visualization;

import java.util.List;

@Dao
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
    Visualization getVisualizationById(Long id);

    @Query("SELECT * FROM visualizations WHERE title = :title")
    List<Visualization> getByTitle(String title);

    @Query("SELECT * FROM visualizations WHERE activated = 1")
    List<Visualization> getActivatedVisualization();

    @Query("UPDATE visualizations SET activated = 0")
    void deactivate();

    @Query("UPDATE visualizations SET activated = 1 WHERE id = :id")
    void activate(Long id);

    @Query("SELECT * FROM visualizations WHERE id IN (:ids)")
    List<Visualization> getByIds(List<Long> ids);

    @Query("SELECT * FROM visualizations WHERE esp_title = :espPacketTitle")
    List<Visualization> getByESPPacketTitle(String espPacketTitle);

    @Transaction()
    default void setActivated(Long id) {
        deactivate();
        activate(id);
    }

}
