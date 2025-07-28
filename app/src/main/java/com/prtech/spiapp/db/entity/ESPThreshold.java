package com.prtech.spiapp.db.entity;

import java.util.List;

public class ESPThreshold {

    private Integer order;
    private Integer initialValue;
    private Integer upperLimit;
    private Integer lowerLimit;
    private Integer thresholdsEnabled;
    private Integer outliersEnabled;
    private List<CommandThreshold> commandThresholds;
    private Integer outlier;
    private String compare;

    public ESPThreshold(int order, int initialValue, int upperLimit, int lowerLimit, int thresholdsEnabled, int outliersEnabled, List<CommandThreshold> commandThresholds, int outlier, String compare ) {
        this.order = order;
        this.initialValue = initialValue;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.thresholdsEnabled = thresholdsEnabled;
        this.outliersEnabled = outliersEnabled;
        this.commandThresholds = commandThresholds;
        this.outlier = outlier;
        this.compare = compare;
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

    public Integer getThresholdsEnabled() {
        return thresholdsEnabled;
    }

    public void setThresholdsEnabled(Integer thresholdsEnabled) {
        this.thresholdsEnabled = thresholdsEnabled;
    }

    public Integer getOutliersEnabled() {
        return outliersEnabled;
    }

    public void setOutliersEnabled(Integer outliersEnabled) {
        this.outliersEnabled = outliersEnabled;
    }

    public List<CommandThreshold> getCommandThresholds() {
        return commandThresholds;
    }

    public void setCommandThresholds(List<CommandThreshold> commandThresholds) {
        this.commandThresholds = commandThresholds;
    }

    public Integer getOutlier() {
        return outlier;
    }

    public void setOutlier(Integer outlier) {
        this.outlier = outlier;
    }

    public String getCompare() {
        return compare;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }
}
