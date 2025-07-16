package com.example.myapplication.db.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.db.converters.CommandThresholdConverter;
import com.example.myapplication.db.converters.ESPTXOutlierConverter;

import java.util.List;

@Entity(tableName="esp_tx")
public class ESPTX {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "sensor_actuator_id")
    private Long sensorActuatorId;

    @ColumnInfo(name = "outliers")
    @TypeConverters(ESPTXOutlierConverter.class)
    private List<ESPTXOutlier> outliers;

    @ColumnInfo(name="deleted")
    private Integer deleted;

    @ColumnInfo(name="created_at")
    private Long created_at;

    @ColumnInfo(name="updated_at")
    private Long updated_at;

    @ColumnInfo(name="deleted_at")
    private Long deleted_at;

    public ESPTX(Long sensorActuatorId, List<ESPTXOutlier> outliers, Integer deleted, Long created_at, Long updated_at) {
        this.sensorActuatorId = sensorActuatorId;
        this.outliers = outliers;
        this.deleted = deleted;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSensorActuatorId() {
        return sensorActuatorId;
    }

    public void setSensorActuatorId(Long sensorActuatorId) {
        this.sensorActuatorId = sensorActuatorId;
    }

    public List<ESPTXOutlier> getOutliers() {
        return outliers;
    }

    public void setOutliers(List<ESPTXOutlier> outliers) {
        this.outliers = outliers;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    public Long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Long updated_at) {
        this.updated_at = updated_at;
    }

    public Long getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(Long deleted_at) {
        this.deleted_at = deleted_at;
    }
}
