package com.example.myapplication.utils.communications.tcp;

import com.example.myapplication.utils.TCPHelper;
import com.example.myapplication.utils.communications.MessageWrapper;
import com.google.gson.Gson;
import com.example.myapplication.db.entity.SensorActuator;

public class TCPActuator {

    private final TCPHelper tcpHelper;
    private final Gson gson = new Gson();

    public TCPActuator(TCPHelper tcpHelper) {
        this.tcpHelper = tcpHelper;
    }

    /**
     * Sends a SensorActuator object to the ESP32 via TCP as JSON.
     */
    public void sendActuatorSetting(SensorActuator actuator) {
        String json = gson.toJson(createMessageWrapper(actuator));
        tcpHelper.sendMessage(json);
    }

    /**
     * Wraps the actuator in a type-tagged message (recommended by spec).
     */
    private MessageWrapper createMessageWrapper(SensorActuator actuator) {
        MessageWrapper wrapper = new MessageWrapper("actuator_setting", System.currentTimeMillis(), actuator);
        return wrapper;
    }

    /**
     * MessageWrapper for consistent communication with ESP32.
     */

}
