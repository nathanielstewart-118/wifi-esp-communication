package com.prtech.spiapp.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.prtech.spiapp.db.converters.ExperimentCommandsConverter;

import java.util.List;

@Entity(tableName="experiments")
public class Experiment {
    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name="experiment_id")
    public String experimentId;

    @ColumnInfo(name="commands")
    @TypeConverters(ExperimentCommandsConverter.class)
    public List<String> commands;

    @ColumnInfo(name="experiment_set")
    public String experimentSet;

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

    public Experiment(String experimentId, String experimentSet, List<String> commands, Integer numberOfTrials, Float preRun, Float postRun, Float rest, Float restRandom) {
        this.experimentId = experimentId;
        this.commands = commands;
        this.numberOfTrials = numberOfTrials;
        this.preRun = preRun;
        this.postRun = postRun;
        this.rest = rest;
        this.restRandom = restRandom;
    }

    public void copyFrom(Experiment other) {
        this.id = other.getId();
        this.experimentId = other.getExperimentId();
        this.experimentSet = other.getExperimentSet();
        this.commands = other.getCommands();
        this.numberOfTrials = other.getNumberOfTrials();
        this.command = other.getCommand();
        this.preRun = other.getPreRun();
        this.postRun = other.getPostRun();
        this.rest = other.getRest();
        this.restRandom = other.getRestRandom();
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

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
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
