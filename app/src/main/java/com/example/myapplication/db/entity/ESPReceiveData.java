package com.example.myapplication.db.entity;

import java.util.ArrayList;
import java.util.List;

public class ESPReceiveData {
    public byte cmd;
    public List<Integer> values = new ArrayList<>();
    public float temp1;
    public float temp2;
    public int counter;
    public byte crc;

    @Override
    public String toString() {
        return "ESPData{" +
                "cmd=" + cmd +
                ", pwm=" + values.stream().toString() +
                ", temp1=" + temp1 +
                ", temp2=" + temp2 +
                ", counter=" + counter +
                ", crc=" + crc +
                '}';
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public float getTemp1() {
        return temp1;
    }

    public void setTemp1(float temp1) {
        this.temp1 = temp1;
    }

    public float getTemp2() {
        return temp2;
    }

    public void setTemp2(float temp2) {
        this.temp2 = temp2;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public byte getCrc() {
        return crc;
    }

    public void setCrc(byte crc) {
        this.crc = crc;
    }
}

