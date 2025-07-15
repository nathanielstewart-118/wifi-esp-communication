package com.example.myapplication.utils.communications.tcp;

import com.example.myapplication.utils.TCPHelper;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.utils.communications.MessageWrapper;
import com.example.myapplication.utils.communications.ucp.UDPActuator;
import com.google.gson.Gson;

public class TCPSensor {

    private final TCPHelper tcpHelper;
    private final Gson gson = new Gson();

    public TCPSensor(TCPHelper tcpHelper) {
        this.tcpHelper = tcpHelper;
    }

    public void sendSensorSetting(SensorActuator sensor) {
        String json = gson.toJson(createMessageWrapper(sensor));
        tcpHelper.sendMessage(json);
    }

    private MessageWrapper<SensorActuator> createMessageWrapper(SensorActuator sensor) {
        MessageWrapper<SensorActuator> wrapper = new MessageWrapper<>("sensor Setting", System.currentTimeMillis(), sensor);
        return wrapper;
    }
}
