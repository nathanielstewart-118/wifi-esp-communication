package com.prtech.spiapp.db.entity;

public class VisualizationRange {
    private Long espPacketId;
    private Integer visualizationType;
    private Integer yAxisRange;
    private Float yStart;
    private Float yEnd;
    private Long upperLimit;
    private Long lowerLimit;

    public VisualizationRange(Long espPacketId, Integer yAxisRange, Integer visualizationType, Float yStart, Float yEnd, Long upperLimit, Long lowerLimit) {
        this.espPacketId = espPacketId;
        this.yAxisRange = yAxisRange;
        this.visualizationType = visualizationType;
        this.yStart = yStart;
        this.yEnd = yEnd;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }

    public Long getEspPacketId() {
        return espPacketId;
    }

    public void setEspPacketId(Long espPacketId) {
        this.espPacketId = espPacketId;
    }

    public Integer getVisualizationType() {
        return visualizationType;
    }

    public Integer getyAxisRange() {
        return yAxisRange;
    }

    public void setyAxisRange(Integer yAxisRange) {
        this.yAxisRange = yAxisRange;
    }

    public void setVisualizationType(Integer visualizationType) {
        this.visualizationType = visualizationType;
    }

    public Float getyStart() {
        return yStart;
    }

    public void setyStart(Float yStart) {
        this.yStart = yStart;
    }

    public Float getyEnd() {
        return yEnd;
    }

    public void setyEnd(Float yEnd) {
        this.yEnd = yEnd;
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
