package com.prtech.spiapp.db.entity;

import java.util.List;

public class ESPThreshold {

    private Integer order;
    private Float initialValue;
    private Float upperLimit;
    private Float lowerLimit;
    private Integer thresholdsEnabled;
    private Integer outliersEnabled;
    private List<CommandThreshold> commandThresholds;
    private Float outlier;
    private String compare;

    public ESPThreshold(Integer order, Float initialValue, Float upperLimit, Float lowerLimit, Integer thresholdsEnabled, Integer outliersEnabled, List<CommandThreshold> commandThresholds, Float outlier, String compare ) {
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

    public Float getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Float initialValue) {
        this.initialValue = initialValue;
    }

    public Float getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Float upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Float getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Float lowerLimit) {
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

    public Float getOutlier() {
        return outlier;
    }

    public void setOutlier(Float outlier) {
        this.outlier = outlier;
    }

    public String getCompare() {
        return compare;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }
}
