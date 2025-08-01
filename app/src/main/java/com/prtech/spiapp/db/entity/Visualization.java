package com.prtech.spiapp.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.VisualizationRangeConverter;

import java.util.List;

@Entity(tableName = "visualizations")
public class Visualization {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name="title")
    private String title;

    @ColumnInfo(name="esp_title")
    private String espTitle;

    @ColumnInfo(name="sample_rate")
    private Integer sampleRate;

    @ColumnInfo(name="block_size")
    private Integer blockSize;

    @ColumnInfo(name="buffer_size")
    private Integer bufferSize;

    @ColumnInfo(name="ranges")
    @TypeConverters(VisualizationRangeConverter.class)
    private List<VisualizationRange> ranges;

    @ColumnInfo(name="save_format")
    private Integer saveFormat; //

    @ColumnInfo(name="save_path")
    public String savePath;

    @ColumnInfo(name="activated")
    private Integer activated;

    @ColumnInfo(name="created_at")
    private Long createdAt;

    @ColumnInfo(name="updated_at")
    private Long updatedAt;


    public Visualization(String title, String espTitle, Integer sampleRate, Integer blockSize, Integer bufferSize, List<VisualizationRange> ranges, Integer saveFormat, String savePath, Integer activated, Long createdAt) {
        this.title = title;
        this.espTitle = espTitle;
        this.sampleRate = sampleRate;
        this.blockSize = blockSize;
        this.bufferSize = bufferSize;
        this.ranges = ranges;
        this.saveFormat = saveFormat;
        this.savePath = savePath;
        this.activated = activated;
        this.createdAt = createdAt;
    }

    public void copyFrom(Visualization other) {
        this.id = other.getId();
        this.title = other.getTitle();
        this.espTitle = other.getEspTitle();
        this.sampleRate = other.getSampleRate();
        this.blockSize = other.getBlockSize();
        this.bufferSize = other.getBufferSize();
        this.ranges = other.getRanges();
        this.activated = other.getActivated();
        this.saveFormat = other.getSaveFormat();
        this.savePath = other.getSavePath();
        this.createdAt = other.getCreatedAt();
        this.updatedAt = other.getUpdatedAt();
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

    public String getEspTitle() {
        return espTitle;
    }

    public void setEspTitle(String espTitle) {
        this.espTitle = espTitle;
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
