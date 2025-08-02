package com.prtech.spiapp.utils.communications;

import com.prtech.spiapp.db.entity.CommandThresholdWithDataType;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.ESPReceiveData;
import com.prtech.spiapp.db.entity.ESPSendData;
import com.prtech.spiapp.db.entity.RangeDTO;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<Long, Object> parse(List<ESPPacket> variables, byte[] data) {
        Map<Long, Object> result = new LinkedHashMap<>();
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int cmd = Byte.toUnsignedInt(buffer.get());
        try {
            for (ESPPacket var : variables) {
                List<Object> values = new ArrayList<>();

                for (int i = 0; i < var.getNumberOfChannels(); i++) {
                    switch (var.getDataType()) {
                        case "uint8":
                            values.add(Byte.toUnsignedInt(buffer.get()));
                            break;
                        case "int8":
                            values.add(buffer.get());
                            break;
                        case "uint16":
                            values.add(Short.toUnsignedInt(buffer.getShort()));
                            break;
                        case "int16":
                            values.add(buffer.getShort());
                            break;
                        case "uint24":
                            values.add(toUnsigned24(buffer));
                            break;
                        case "int24":
                            values.add(toSigned24(buffer));
                            break;
                        case "uint32":
                            values.add(Integer.toUnsignedLong(buffer.getInt()));
                            break;
                        case "int32":
                            values.add(buffer.getInt());
                            break;
                        case "float":
                            values.add(buffer.getFloat());
                            break;
                        case "double":
                            values.add(buffer.getDouble());
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown type: " + var.getDataType());
                    }
                }
                result.put(var.getId(), values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static int toUnsigned24(ByteBuffer buffer) {
        int b1 = Byte.toUnsignedInt(buffer.get());
        int b2 = Byte.toUnsignedInt(buffer.get());
        int b3 = Byte.toUnsignedInt(buffer.get());
        return (b3 << 16) | (b2 << 8) | b1;
    }

    private static int toSigned24(ByteBuffer buffer) {
        int b1 = Byte.toUnsignedInt(buffer.get());
        int b2 = Byte.toUnsignedInt(buffer.get());
        int b3 = buffer.get(); // signed high byte
        return (b3 << 16) | (b2 << 8) | b1;
    }

    public static byte[] encodeCommand(ESPSendData espSendData) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(hexStringToByteArray(espSendData.getCommandCode()));
        getByte(espSendData.getTime1(), "float", buffer);
        getByte(espSendData.getTime2(), "float", buffer);
        List<CommandThresholdWithDataType> dtos = espSendData.getThresholds();
        for (int i = 0; i < dtos.size(); i++) {
            String type = dtos.get(i).getDataType().toLowerCase();
            int nChannels = dtos.get(i).getThresholds().size();
            for (int j = 0; j < nChannels; j ++) {
                int value = dtos.get(i).getThresholds().get(j);
                    getByte((Object) value, type, buffer);
            }
        }

        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    private static void getByte(Object value, String dataType, ByteBuffer buffer) {
        switch (dataType) {
            case "uint8":
            case "int8":
                buffer.put(((Number) value).byteValue());
                break;

            case "uint16":
            case "int16":
                buffer.putShort(((Number) value).shortValue());
                break;

            case "uint24":
            case "int24":
                int int24 = ((Number) value).intValue();
                buffer.put((byte) (int24 & 0xFF));
                buffer.put((byte) ((int24 >> 8) & 0xFF));
                buffer.put((byte) ((int24 >> 16) & 0xFF));
                break;

            case "uint32":
            case "int32":
                buffer.putInt(((Number) value).intValue());
                break;

            case "float":
                buffer.putFloat(((Number) value).floatValue());
                break;

            case "double":
                buffer.putDouble(((Number) value).doubleValue());
                break;

            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

    public static byte[] hexStringToByteArray(String hexStr) {
        if (hexStr.startsWith("0x") || hexStr.startsWith("0X")) {
            hexStr = hexStr.substring(2); // Remove "0x" prefix
        }
        // Ensure even length
        if (hexStr.length() % 2 != 0) {
            hexStr = "0" + hexStr;
        }

        int len = hexStr.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);
        }
        return data;
    }

}

