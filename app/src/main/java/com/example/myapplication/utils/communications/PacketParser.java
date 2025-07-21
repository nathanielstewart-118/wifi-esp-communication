package com.example.myapplication.utils.communications;

import com.example.myapplication.db.entity.ESPReceiveData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketParser {

    public static ESPReceiveData parseESPData(byte[] data, int varLength, int length) {
        if (length < 17) return null; // Minimum length check

        ESPReceiveData result = new ESPReceiveData();
        int index = 0;

        result.cmd = data[index++];

        for (int i = 0; i < varLength; i++) {
            result.values.add(data[index++] & 0xFF);
        }

        result.temp1 = ByteBuffer.wrap(data, index, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        index += 4;

        result.temp2 = ByteBuffer.wrap(data, index, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        index += 4;

        result.counter = ((data[index++] & 0xFF) | ((data[index++] & 0xFF) << 8));

        result.crc = data[index];

        return result;
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
        }
        return sb.toString().trim();
    }

    private byte[] buildESPDataToSend(
            byte cmd,
            int time1,     // uint16
            int time2,     // uint16
            int[] pwm,     // 6 x uint8
            float temp1,   // float32
            float temp2    // float32
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(19);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // ESP usually expects little-endian

        buffer.put(cmd);
        buffer.putShort((short) time1);
        buffer.putShort((short) time2);

        for (int i = 0; i < 6; i++) {
            buffer.put((byte) pwm[i]);
        }

        buffer.putFloat(temp1);
        buffer.putFloat(temp2);

        return buffer.array();
    }

}

