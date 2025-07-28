package com.prtech.spiapp.utils.communications.tcp;

import com.prtech.spiapp.utils.TCPHelper;
import com.prtech.spiapp.utils.communications.MessageWrapper;
import com.google.gson.Gson;
import com.prtech.spiapp.db.entity.ESPPacket;

public class TCPActuator {

    private final TCPHelper tcpHelper;
    private final Gson gson = new Gson();

    public TCPActuator(TCPHelper tcpHelper) {
        this.tcpHelper = tcpHelper;
    }

    /**
     * Sends a SensorActuator object to the ESP32 via TCP as JSON.
     */
    public void sendActuatorSetting(ESPPacket actuator) {
        String json = gson.toJson(createMessageWrapper(actuator));
        tcpHelper.sendMessage(json);
    }

    /**
     * Wraps the actuator in a type-tagged message (recommended by spec).
     */
    private MessageWrapper createMessageWrapper(ESPPacket actuator) {
        MessageWrapper wrapper = new MessageWrapper("actuator_setting", System.currentTimeMillis(), actuator);
        return wrapper;
    }

    /**
     * MessageWrapper for consistent communication with ESP32.
     */

}
