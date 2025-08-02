package com.prtech.spiapp.db.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="monitorings")
public class Monitoring {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name="visualization_id")
    private Long visualizationId;

    @ColumnInfo(name="data")
    private String data;

    @ColumnInfo(name="created_at")
    private Long created_at;

    @ColumnInfo(name="updated_at")
    private Long updated_at;

    public Monitoring(String data, Long created_at) {
        this.data = data;
        this.created_at = created_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVisualizationId() {
        return visualizationId;
    }

    public void setVisualizationId(Long visualizationId) {
        this.visualizationId = visualizationId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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
}
