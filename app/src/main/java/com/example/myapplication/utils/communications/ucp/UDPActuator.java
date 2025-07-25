package com.example.myapplication.utils.communications.ucp;

import com.example.myapplication.utils.UDPHelper;
import com.example.myapplication.db.entity.SensorActuator;
import com.google.gson.Gson;

public class UDPActuator {

    private final UDPHelper udpHelper;
    private final String targetIp;
    private final int targetPort;
    private final Gson gson = new Gson();

    public UDPActuator(UDPHelper udpHelper, String targetIp, int targetPort) {
        this.udpHelper = udpHelper;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
    }

    /**
     * Sends a SensorActuator object to the ESP32 via UDP as JSON.
     */
    public void sendActuatorSetting(SensorActuator actuator) {
        String json = gson.toJson(createMessageWrapper(actuator));
        udpHelper.sendUdpMessage(targetIp, targetPort, json);
    }

    /**
     * Wraps the actuator data in a tagged message for ESP recognition.
     */
    private MessageWrapper createMessageWrapper(SensorActuator actuator) {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.type = "actuator_setting";
        wrapper.timestamp = System.currentTimeMillis();
        wrapper.payload = actuator;
        return wrapper;
    }

    /**
     * Inner wrapper class for clean JSON structure.
     */
    private static class MessageWrapper {
        String type;
        long timestamp;
        SensorActuator payload;
    }
}

