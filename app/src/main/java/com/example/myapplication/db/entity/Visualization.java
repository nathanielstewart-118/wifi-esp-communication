package com.example.myapplication.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visualizations")
public class Visualization {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name="sample_rate")
    public Float sampleRate;

    @ColumnInfo(name="block_size")
    public Integer blockSize;

    @ColumnInfo(name="buffer_size")
    public Integer bufferSize;

    @ColumnInfo(name="auto_y_axis_range")
    public Boolean autoYAxisRange;

    @ColumnInfo(name="y_min")
    public Float yAxisMin;

    @ColumnInfo(name="y_max")
    public Float yAxisMax;

    @ColumnInfo(name="x_secs")
    public Boolean xAxisInSeconds; // true = seconds, false = samples

    @ColumnInfo(name="save_format")
    public String saveFormat; //

    public Visualization(Float sampleRate, Integer blockSize, Integer bufferSize, Boolean autoYAxisRange, Float yAxisMin, Float yAxisMax, Boolean xAxisInSeconds, String saveFormat) {
        this.sampleRate = sampleRate;
        this.blockSize = blockSize;
        this.bufferSize = bufferSize;
        this.autoYAxisRange = autoYAxisRange;
        this.yAxisMin = yAxisMin;
        this.yAxisMax = yAxisMax;
        this.xAxisInSeconds = xAxisInSeconds;
        this.saveFormat = saveFormat;
    }

    public Float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getAutoYAxisRange() {
        return autoYAxisRange;
    }

    public void setAutoYAxisRange(Boolean autoYAxisRange) {
        this.autoYAxisRange = autoYAxisRange;
    }

    public Float getyAxisMin() {
        return yAxisMin;
    }

    public void setyAxisMin(Float yAxisMin) {
        this.yAxisMin = yAxisMin;
    }

    public Float getyAxisMax() {
        return yAxisMax;
    }

    public void setyAxisMax(Float yAxisMax) {
        this.yAxisMax = yAxisMax;
    }

    public Boolean getxAxisInSeconds() {
        return xAxisInSeconds;
    }

    public void setxAxisInSeconds(Boolean xAxisInSeconds) {
        this.xAxisInSeconds = xAxisInSeconds;
    }

    public String getSaveFormat() {
        return saveFormat;
    }

    public void setSaveFormat(String saveFormat) {
        this.saveFormat = saveFormat;
    }
}
