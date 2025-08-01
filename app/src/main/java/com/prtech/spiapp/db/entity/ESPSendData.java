package com.prtech.spiapp.db.entity;

import java.util.List;

public class ESPSendData {
    String commandCode;
    Float time1;
    Float time2;

    List<CommandThresholdWithDataType> thresholds;

    public ESPSendData(String commandCode, Float time1, Float time2, List<CommandThresholdWithDataType> thresholds) {
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

    public List<CommandThresholdWithDataType> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<CommandThresholdWithDataType> thresholds) {
        this.thresholds = thresholds;
    }
}
