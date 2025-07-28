package com.prtech.spiapp.db.entity;

import java.util.List;

public class CommandThreshold {

    private Long sensorActuatorId;
    private List<Double> thresholds;

    public Long getSensorActuatorId() {
        return sensorActuatorId;
    }

    public void setSensorActuatorId(Long sensorActuatorId) {
        this.sensorActuatorId = sensorActuatorId;
    }

    public List<Double> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<Double> thresholds) {
        this.thresholds = thresholds;
    }

}
