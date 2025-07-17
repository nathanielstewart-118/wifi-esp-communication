package com.example.myapplication.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.db.converters.ESPRXRTThresholdConverter;
import com.example.myapplication.db.converters.ESPTXOutlierConverter;

import java.util.List;

@Entity(tableName="esp_rxrt")
public class ESPRXRT {
    @PrimaryKey(autoGenerate = true)
    private Long id;

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
    @TypeConverters(ESPRXRTThresholdConverter.class)
    private List<ESPRXRTThreshold> thresholds;

    @ColumnInfo(name="deleted")
    private Integer deleted;

    @ColumnInfo(name="created_at")
    private Long created_at;

    @ColumnInfo(name="updated_at")
    private Long updated_at;

    @ColumnInfo(name="deleted_at")
    private Long deleted_at;


    public ESPRXRT(Long sensorActuatorId, Integer sensorOrActuator, String variableName, String dataType, Integer numberOfChannels, List<ESPRXRTThreshold> thresholds, Integer deleted, Long created_at, Long updated_at) {
        this.sensorActuatorId = sensorActuatorId;
        this.sensorOrActuator = sensorOrActuator;
        this.variableName = variableName;
        this.dataType = dataType;
        this.numberOfChannels = numberOfChannels;
        this.thresholds = thresholds;
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


    public List<ESPRXRTThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<ESPRXRTThreshold> thresholds) {
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
