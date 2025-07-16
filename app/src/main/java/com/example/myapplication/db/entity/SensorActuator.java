package com.example.myapplication.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "sensors_and_actuators")
public class SensorActuator {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "variable_name")
    private String variableName;

    @ColumnInfo(name = "data_type")
    private String dataType;

    @ColumnInfo(name = "sensor_or_actuator")
    private Integer sensorOrActuator;

    @ColumnInfo(name = "number_of_channels")
    private Integer numberOfChannels;

    @ColumnInfo(name = "monitoring")
    private Integer monitoring;

    @ColumnInfo(name = "real_time_control")
    private Integer realTimeControl;

    public SensorActuator(String variableName, Integer sensorOrActuator, String dataType, Integer numberOfChannels, Integer monitoring, Integer realTimeControl) {
        this.variableName = variableName;
        this.dataType = dataType;
        this.numberOfChannels = numberOfChannels;
        this.monitoring = monitoring;
        this.realTimeControl = realTimeControl;
        this.sensorOrActuator = sensorOrActuator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setNumberOfChannels(Integer numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public Integer getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Integer monitoring) {
        this.monitoring = monitoring;
    }

    public Integer getRealTimeControl() {
        return realTimeControl;
    }

    public void setRealTimeControl(Integer realTimeControl) {
        this.realTimeControl = realTimeControl;
    }
}
