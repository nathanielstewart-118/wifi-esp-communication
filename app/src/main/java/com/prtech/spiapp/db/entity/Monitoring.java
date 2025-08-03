package com.prtech.spiapp.db.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.MapConverter;

import java.util.Map;

@Entity(tableName="monitorings")
public class Monitoring {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name="esp_packet_title")
    private String espPacketTitle;

    @ColumnInfo(name="command_title")
    private String commandTitle;

    @ColumnInfo(name="data")
    private String data;

    @ColumnInfo(name="esp_visualization_map")
    @TypeConverters(MapConverter.class)
    private Map<Long, Long> espVisualizationMap;

    @ColumnInfo(name="created_at")
    private Long createdAt;

    @ColumnInfo(name="updated_at")
    private Long updatedAt;

    public Monitoring(String espPacketTitle, String commandTitle, String data, Map<Long, Long> espVisualizationMap, Long createdAt, Long updatedAt) {
        this.espPacketTitle = espPacketTitle;
        this.commandTitle = commandTitle;
        this.data = data;
        this.espVisualizationMap = espVisualizationMap;
        if(createdAt != null) this.createdAt = createdAt;
        if(updatedAt != null) this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEspPacketTitle() {
        return espPacketTitle;
    }

    public void setEspPacketTitle(String espPacketTitle) {
        this.espPacketTitle = espPacketTitle;
    }

    public String getCommandTitle() {
        return commandTitle;
    }

    public void setCommandTitle(String commandTitle) {
        this.commandTitle = commandTitle;
    }

    public Map<Long, Long> getEspVisualizationMap() {
        return espVisualizationMap;
    }

    public void setEspVisualizationMap(Map<Long, Long> espVisualizationMap) {
        this.espVisualizationMap = espVisualizationMap;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
