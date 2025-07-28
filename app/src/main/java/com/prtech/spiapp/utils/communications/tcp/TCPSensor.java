package com.prtech.spiapp.utils.communications.tcp;

import com.prtech.spiapp.utils.TCPHelper;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.utils.communications.MessageWrapper;
import com.google.gson.Gson;

public class TCPSensor {

    private final TCPHelper tcpHelper;
    private final Gson gson = new Gson();

    public TCPSensor(TCPHelper tcpHelper) {
        this.tcpHelper = tcpHelper;
    }

    public void sendSensorSetting(ESPPacket sensor) {
        String json = gson.toJson(createMessageWrapper(sensor));
        tcpHelper.sendMessage(json);
    }

    private MessageWrapper<ESPPacket> createMessageWrapper(ESPPacket sensor) {
        MessageWrapper<ESPPacket> wrapper = new MessageWrapper<>("sensor Setting", System.currentTimeMillis(), sensor);
        return wrapper;
    }
}
