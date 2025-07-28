package com.prtech.spiapp.db.entity;

public class ESPOutlier {
    private Integer order;
    private Integer outlier;
    private String comparison;
    private Integer activate;

    public ESPOutlier(Integer order, Integer outlier, String comparison, Integer activate) {
        this.order = order;
        this.outlier = outlier;
        this.comparison = comparison;
        this.activate = activate;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getOutlier() {
        return outlier;
    }

    public void setOutlier(Integer outlier) {
        this.outlier = outlier;
    }

    public String getComparison() {
        return comparison;
    }

    public void setComparison(String comparison) {
        this.comparison = comparison;
    }

    public Integer getActivate() {
        return activate;
    }

    public void setActivate(Integer activate) {
        this.activate = activate;
    }

}