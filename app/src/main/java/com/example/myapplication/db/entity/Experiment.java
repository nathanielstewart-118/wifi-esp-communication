package com.example.myapplication.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="experiments")
public class Experiment {
    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name="experiment_id")
    public String experimentId;

    @ColumnInfo(name="experiment_set")
    public String experimentSet;

    @ColumnInfo(name="command_id")
    public Long commandId;

    @ColumnInfo(name="number_of_trials")
    public Integer numberOfTrials;

    @ColumnInfo(name="command")
    public Float command;

    @ColumnInfo(name="pre_run")
    public Float preRun;

    @ColumnInfo(name="post_run")
    public Float postRun;

    @ColumnInfo(name="rest")
    public Float rest;

    @ColumnInfo(name="rest_random")
    public Float restRandom;

    public Experiment(String experimentId, String experimentSet, Long commandId, Integer numberOfTrials, Float preRun, Float postRun, Float rest, Float restRandom) {
        this.experimentId = experimentId;
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

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentSet() {
        return experimentSet;
    }

    public void setExperimentSet(String experimentSet) {
        this.experimentSet = experimentSet;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Integer getNumberOfTrials() {
        return numberOfTrials;
    }

    public void setNumberOfTrials(Integer numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
    }

    public Float getCommand() {
        return command;
    }

    public void setCommand(Float command) {
        this.command = command;
    }

    public Float getPreRun() {
        return preRun;
    }

    public void setPreRun(Float preRun) {
        this.preRun = preRun;
    }

    public Float getPostRun() {
        return postRun;
    }

    public void setPostRun(Float postRun) {
        this.postRun = postRun;
    }

    public Float getRest() {
        return rest;
    }

    public void setRest(Float rest) {
        this.rest = rest;
    }

    public Float getRestRandom() {
        return restRandom;
    }

    public void setRestRandom(Float restRandom) {
        this.restRandom = restRandom;
    }
}
