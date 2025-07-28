package com.prtech.spiapp.db.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.ESPTXOutlierConverter;

import java.util.List;

@Entity(tableName="esp_tx")
public class ESPTX extends BaseEntity {

    @ColumnInfo(name = "sensor_actuator_id")
    private Long sensorActuatorId;

    @ColumnInfo(name = "outliers")
    @TypeConverters(ESPTXOutlierConverter.class)
    private List<ESPOutlier> outliers;

    @ColumnInfo(name="deleted")
    private Integer deleted;

    @ColumnInfo(name="deleted_at")
    private Long deleted_at;

    public ESPTX(Long sensorActuatorId, List<ESPOutlier> outliers, Integer deleted, Long createdAt, Long updatedAt) {
        this.sensorActuatorId = sensorActuatorId;
        this.outliers = outliers;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public List<ESPOutlier> getOutliers() {
        return outliers;
    }

    public void setOutliers(List<ESPOutlier> outliers) {
        this.outliers = outliers;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Long getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(Long deleted_at) {
        this.deleted_at = deleted_at;
    }
}
