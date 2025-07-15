package com.example.myapplication.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;

import com.example.myapplication.db.converters.CommandThresholdConverter;

import java.util.List;

@Entity(tableName="commands")
public class Command {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "command_code")
    private String commandCode;

    @ColumnInfo(name = "time1")
    private Float time1;

    @ColumnInfo(name = "time2")
    private Float time2;

    @ColumnInfo(name = "thresholds")
    @TypeConverters(CommandThresholdConverter.class)
    private List<CommandThreshold> thresholds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Command(String commandCode, float time1, float time2, List<CommandThreshold> thresholds) {
        this.commandCode = commandCode;
        this.time1 = time1;
        this.time2 = time2;
        this.thresholds = thresholds;
    }

    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
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

    public List<CommandThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<CommandThreshold> thresholds) {
        this.thresholds = thresholds;
    }
}


