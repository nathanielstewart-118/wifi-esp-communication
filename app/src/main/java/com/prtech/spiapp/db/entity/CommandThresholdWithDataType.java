package com.prtech.spiapp.db.entity;

import java.util.List;

public class CommandThresholdWithDataType {

    private Long espPacketId;
    private String dataType;
    private List<Integer> thresholds;

    public CommandThresholdWithDataType(Long espPacketId, String dataType, List<Integer> thresholds) {
        this.espPacketId = espPacketId;
        this.dataType = dataType;
        this.thresholds = thresholds;
    }

    public Long getEspPacketId() {
        return espPacketId;
    }

    public void setEspPacketId(Long espPacketId) {
        this.espPacketId = espPacketId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<Integer> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<Integer> thresholds) {
        this.thresholds = thresholds;
    }

}
