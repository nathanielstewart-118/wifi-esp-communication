package com.prtech.spiapp.db.entity;

public class RangeDTO {


    public Long espPacketId;

    public Long visualizationId;

    public String variableName;

    public String dataType;

    public Integer numberOfChannels;

    public Integer visualizationType;

    public Integer yAxisRange;

    public Long upperLimit;

    public Long lowerLimit;

    public RangeDTO(Long visualizationId, Long espPacketId, String variableName, String dataType, Integer numberOfChannels, Integer visualizationType, Integer yAxisRange, Long upperLimit, Long lowerLimit) {
        this.visualizationId = visualizationId;
        this.espPacketId = espPacketId;
        this.variableName = variableName;
        this.dataType = dataType;
        this.numberOfChannels = numberOfChannels;
        this.visualizationType = visualizationType;
        this.yAxisRange = yAxisRange;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }

    public Long getVisualizationId() {
        return visualizationId;
    }

    public void setVisualizationId(Long visualizationId) {
        this.visualizationId = visualizationId;
    }

    public Long getEspPacketId() {
        return espPacketId;
    }

    public void setEspPacketId(Long espPacketId) {
        this.espPacketId = espPacketId;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(Integer numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public Integer getVisualizationType() {
        return visualizationType;
    }

    public void setVisualizationType(Integer visualizationType) {
        this.visualizationType = visualizationType;
    }

    public Integer getyAxisRange() {
        return yAxisRange;
    }

    public void setyAxisRange(Integer yAxisRange) {
        this.yAxisRange = yAxisRange;
    }

    public Long getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Long upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Long getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Long lowerLimit) {
        this.lowerLimit = lowerLimit;
    }
}
