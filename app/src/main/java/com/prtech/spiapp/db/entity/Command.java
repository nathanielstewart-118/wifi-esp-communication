package com.prtech.spiapp.db.entity;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.CommandThresholdConverter;

import java.util.List;

@Entity(tableName="commands")
public class Command extends BaseEntity {

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "command_code")
    private String commandCode;

    @ColumnInfo(name = "time1")
    private Float time1;

    @ColumnInfo(name = "time2")
    private Float time2;

    @ColumnInfo(name = "activated")
    private Integer activated;

    @ColumnInfo(name = "thresholds")
    @TypeConverters(CommandThresholdConverter.class)
    private List<CommandThreshold> thresholds;


    public Command(String title, String commandCode, float time1, float time2, Integer displayOrder, Integer activated, List<CommandThreshold> thresholds) {
        this.commandCode = commandCode;
        this.title = title;
        this.time1 = time1;
        this.time2 = time2;
        this.displayOrder = displayOrder;
        this.thresholds = thresholds;
        this.activated = activated;
        this.createdAt = System.currentTimeMillis();
    }

    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Float getTime1() {
        return time1;
    }

    public void setTime1(Float time1) {
        this.time1 = time1;
    }

    public Float getTime2() {
        return time2;
    }

    public void setTime2(Float time2) {
        this.time2 = time2;
    }

    public Integer getActivated() {
        return activated;
    }

    public void setActivated(Integer activated) {
        this.activated = activated;
    }

    public List<CommandThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<CommandThreshold> thresholds) {
        this.thresholds = thresholds;
    }

}


