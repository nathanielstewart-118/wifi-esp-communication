package com.example.myapplication.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="experiments")
public class Experiment {
    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name="command_id")
    public String commandId;

    @ColumnInfo(name="number_of_trials")
    public Integer numberOfTrials;

    @ColumnInfo(name="pre_run")
    public Float preRun;

    @ColumnInfo(name="post_run")
    public float postRun;

    @ColumnInfo(name="rest")
    public float rest;

    @ColumnInfo(name="rest_random")
    public float restRandom;

    public Experiment(String commandId, Integer numberOfTrials, Float preRun, float postRun, float rest, float restRandom) {
        this.commandId = commandId;
        this.numberOfTrials = numberOfTrials;
        this.preRun = preRun;
        this.postRun = postRun;
        this.rest = rest;
        this.restRandom = restRandom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public Integer getNumberOfTrials() {
        return numberOfTrials;
    }

    public void setNumberOfTrials(Integer numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
    }

    public Float getPreRun() {
        return preRun;
    }

    public void setPreRun(Float preRun) {
        this.preRun = preRun;
    }

    public float getPostRun() {
        return postRun;
    }

    public void setPostRun(float postRun) {
        this.postRun = postRun;
    }

    public float getRest() {
        return rest;
    }

    public void setRest(float rest) {
        this.rest = rest;
    }

    public float getRestRandom() {
        return restRandom;
    }

    public void setRestRandom(float restRandom) {
        this.restRandom = restRandom;
    }
}
