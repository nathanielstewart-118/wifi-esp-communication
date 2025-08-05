package com.prtech.spiapp.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.ESPThresholdConverter;

import java.util.List;


@Entity(tableName = "esp_packets")
public class ESPPacket extends BaseEntity {

    @ColumnInfo(name="title")
    private String title;

    @ColumnInfo(name = "variable_name")
    private String variableName;

    @ColumnInfo(name = "data_type")
    private String dataType;

    @ColumnInfo(name = "sensor_or_actuator")
    private Integer sensorOrActuator;

    @ColumnInfo(name = "number_of_channels")
    private Integer numberOfChannels;

    @ColumnInfo(name="crc")
    private Integer crc;

    @ColumnInfo(name = "monitoring")
    private Integer monitoring;

    @ColumnInfo(name = "real_time_control")
    private Integer realTimeControl;

    @ColumnInfo(name="thresolds")
    @TypeConverters(ESPThresholdConverter.class)
    private List<ESPThreshold> thresholds;


    public ESPPacket(String title, String variableName, Integer sensorOrActuator, String dataType, Integer numberOfChannels, Integer monitoring, Integer realTimeControl, Long createdAt, List<ESPThreshold> thresholds) {
        this.variableName = variableName;
        this.title = title;
        this.dataType = dataType;
        this.numberOfChannels = numberOfChannels;
        this.monitoring = monitoring;
        this.realTimeControl = realTimeControl;
        this.sensorOrActuator = sensorOrActuator;
        this.createdAt = createdAt;
        this.thresholds = thresholds;
    }

    public String getAutoCompleteText() {
        return this.title;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getCrc() {
        return crc;
    }

    public void setCrc(Integer crc) {
        this.crc = crc;
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

    public List<ESPThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<ESPThreshold> thresholds) {
        this.thresholds = thresholds;
    }
}
