package com.prtech.spiapp.db.entity;

import java.util.List;

public class CommandThreshold {

    private Long espPacketId;
    private List<Integer> thresholds;

    public CommandThreshold(Long espPacketId, List<Integer> thresholds) {
        this.espPacketId = espPacketId;
        this.thresholds = thresholds;
    }

    public Long getEspPacketId() {
        return espPacketId;
    }

    public void setEspPacketId(Long espPacketId) {
        this.espPacketId = espPacketId;
    }

    public List<Integer> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<Integer> thresholds) {
        this.thresholds = thresholds;
    }
}
