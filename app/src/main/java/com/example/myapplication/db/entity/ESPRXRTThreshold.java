package com.example.myapplication.db.entity;

import com.example.myapplication.settings.ESPRXRTSetting;

public class ESPRXRTThreshold {

    private Integer order;
    private Integer initialValue;
    private Integer upperLimit;
    private Integer lowerLimit;
    private Integer active;

    public ESPRXRTThreshold(int order, int initialValue, int upperLimit, int lowerLimit, int active ) {
        this.order = order;
        this.initialValue = initialValue;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.active = active;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Integer initialValue) {
        this.initialValue = initialValue;
    }

    public Integer getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Integer upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Integer getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Integer lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }
}
