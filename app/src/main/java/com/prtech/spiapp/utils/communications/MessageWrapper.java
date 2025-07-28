package com.prtech.spiapp.utils.communications;

import com.prtech.spiapp.db.entity.ESPPacket;

public class MessageWrapper<T> {
    private String type;               // e.g., "actuator_setting"
    private Long timestamp;           // optional for sync/logging
    private ESPPacket payload;   // actual actuator data

    public MessageWrapper(String type, Long timestamp, ESPPacket payload) {
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

    public ESPPacket getPayload() {
        return payload;
    }

    public void setPayload(ESPPacket payload) {
        this.payload = payload;
    }
}
