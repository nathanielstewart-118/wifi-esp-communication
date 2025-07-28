package com.prtech.spiapp.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.ESPThresholdConverter;

import java.util.List;

@Entity(tableName="esp_rxrt")
public class ESPRXRT extends BaseEntity {

    @ColumnInfo(name = "sensor_actuator_id")
    private Long sensorActuatorId;

    @ColumnInfo(name = "sensor_or_actuator")
    private Integer sensorOrActuator;

    @ColumnInfo(name = "variable_name")
    private String variableName;

    @ColumnInfo(name = "data_type")
    private String dataType;

    @ColumnInfo(name = "number_of_channels")
    private Integer numberOfChannels;

    @ColumnInfo(name = "thresholds")
    @TypeConverters(ESPThresholdConverter.class)
    private List<ESPThreshold> thresholds;

    @ColumnInfo(name="deleted")
    private Integer deleted;

    @ColumnInfo(name="deleted_at")
    private Long deleted_at;


    public ESPRXRT(Long sensorActuatorId, Integer sensorOrActuator, String variableName, String dataType, Integer numberOfChannels, List<ESPThreshold> thresholds, Integer deleted, Long createdAt) {
        this.sensorActuatorId = sensorActuatorId;
        this.sensorOrActuator = sensorOrActuator;
        this.variableName = variableName;
        this.dataType = dataType;
        this.numberOfChannels = numberOfChannels;
        this.thresholds = thresholds;
        this.deleted = deleted;
        this.createdAt = createdAt;
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

    public Integer getSensorOrActuator() {
        return sensorOrActuator;
    }

    public void setSensorOrActuator(Integer sensorOrActuator) {
        this.sensorOrActuator = sensorOrActuator;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getNumberOfChannels() {
        return numberOfChannels;
    }


    public List<ESPThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<ESPThreshold> thresholds) {
        this.thresholds = thresholds;
    }

    public void setNumberOfChannels(Integer numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
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
