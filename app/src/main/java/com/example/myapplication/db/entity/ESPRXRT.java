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
    private Integer sensorActuatorId;

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

    public ESPRXRT(String variableName, String dataType, Integer numberOfChannels, List<ESPRXRTThreshold> thresholds) {
        this.variableName = variableName;
        this.dataType = dataType;
        this.numberOfChannels = numberOfChannels;
        this.thresholds = thresholds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSensorActuatorId() {
        return sensorActuatorId;
    }

    public void setSensorActuatorId(Integer sensorActuatorId) {
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

    public void setChannelCount(Integer channelCount) {
        this.numberOfChannels = channelCount;
    }

    public List<ESPRXRTThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<ESPRXRTThreshold> thresholds) {
        this.thresholds = thresholds;
    }
}
