package com.example.myapplication.db.entity;

public class SensorActuatorIdWithTitle {
    Long id;
    String title;

    public SensorActuatorIdWithTitle(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getAutoCompleteText() {
        return this.title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
