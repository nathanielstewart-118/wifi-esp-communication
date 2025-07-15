package com.example.myapplication.utils.communications;

import com.example.myapplication.db.entity.SensorActuator;

public class MessageWrapper<T> {
    private String type;               // e.g., "actuator_setting"
    private Long timestamp;           // optional for sync/logging
    private SensorActuator payload;   // actual actuator data

    public MessageWrapper(String type, Long timestamp, SensorActuator payload) {
        this.type = type;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public SensorActuator getPayload() {
        return payload;
    }

    public void setPayload(SensorActuator payload) {
        this.payload = payload;
    }
}
