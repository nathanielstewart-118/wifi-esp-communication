package com.example.myapplication.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.db.converters.ExperimentCommandsConverter;
import com.example.myapplication.db.converters.VisualizationRangeConverter;

import java.util.List;

@Entity(tableName = "visualizations")
public class Visualization {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name="visualization_id")
    public String visualizationId;

    @ColumnInfo(name="sample_rate")
    public Integer sampleRate;

    @ColumnInfo(name="block_size")
    public Integer blockSize;

    @ColumnInfo(name="buffer_size")
    public Integer bufferSize;

    @ColumnInfo(name="ranges")
    @TypeConverters(VisualizationRangeConverter.class)
    List<VisualizationRange> ranges;

    @ColumnInfo(name="save_format")
    public Integer saveFormat; //

    @ColumnInfo(name="save_path")
    public String savePath;

    @ColumnInfo(name="activated")
    public Integer activated;

    @ColumnInfo(name="created_at")
    public Long created_at;

    @ColumnInfo(name="updated_at")
    public Long updated_at;


    public Visualization(String visualizationId, Integer sampleRate, Integer blockSize, Integer bufferSize, List<VisualizationRange> ranges, Integer saveFormat, String savePath, Integer activated, Long created_at) {
        this.visualizationId = visualizationId;
        this.sampleRate = sampleRate;
        this.blockSize = blockSize;
        this.bufferSize = bufferSize;
        this.ranges = ranges;
        this.saveFormat = saveFormat;
        this.savePath = savePath;
        this.activated = activated;
        this.created_at = created_at;
    }

    public void copyFrom(Visualization other) {
        this.id = other.getId();
        this.visualizationId = other.getVisualizationId();
        this.sampleRate = other.getSampleRate();
        this.blockSize = other.getBlockSize();
        this.bufferSize = other.getBufferSize();
        this.ranges = other.getRanges();
        this.activated = other.getActivated();
        this.saveFormat = other.getSaveFormat();
        this.savePath = other.getSavePath();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVisualizationId() {
        return visualizationId;
    }

    public void setVisualizationId(String visualizationId) {
        this.visualizationId = visualizationId;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public List<VisualizationRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<VisualizationRange> ranges) {
        this.ranges = ranges;
    }

    public Integer getSaveFormat() {
        return saveFormat;
    }

    public void setSaveFormat(Integer saveFormat) {
        this.saveFormat = saveFormat;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public Integer getActivated() {
        return activated;
    }

    public void setActivated(Integer activated) {
        this.activated = activated;
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
