package com.prtech.spiapp.utils;

import android.graphics.Color;

public class Constants {

    public static final String LOGGING_BASE_URL = "https://s1385328.eu-nbg-2.betterstackdata.com";
    public static final String LOGGING_BEARER_TOKEN = "S3PiqnfLCUvgY92KcJbcXxaE";
    public static final String LOGGING_REQUEST_METHOD = "POST";
    public static final String[] DATA_TYPES = { "uint8", "int8", "uint16", "int16", "uint24", "int24", "uint32", "int32", "float", "double"};
    public static final String[][] Y_AXIS_RANGES = {
        {"Signed 8 bits, -128 to +127", "-128", "127"},
        {"Signed 16 bits, -32,768 to + 32,767", "-32768", "32767"},
        {"Signed 32 bits, -2,147,483,648 to 2,147,483,647", "-2147483648", "2147483647"},
        {"Unsigned 8 bits, 0 to 255", "0", "255"},
        {"Unsigned 16 bits, 0 to 65,536", "0", "65536"},
        {"Unsigned 32 bits, 0 to 4,294,967,295", "0", "4294967295"},
        {"0 to 1", "0", "1"},
        {"-1 to 1", "-1", "1"}
    };

    public static final String[] VISUALIZATION_OPTIONS = { "Graph", "Table", "Disabled" };
    public static final String tcpServerIp = "192.168.4.1";
//    public static final String tcpServerIp = "192.168.149.232";
    public static final int tcpServerPort = 8080;
//    public static final int tcpServerPort = 9999;
    public static final int udpLocalPort = 9999;
    public static final int udpPort = 10001;
    public static final String[] LANGUAGES = { "en", "cn", "fr", "de" };
    public static final Integer MAX_VARIABLE_NUMBER_IN_PACKET = 100;
    public static final int[] COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.GRAY, Color.BLACK
    };
    public static final String[] TITLES = {
            "WiFi Access and Connection",
            "ESP Packet Settings",
            "Command Settings",
            "Experiment Settings",
            "Visualization Settings",
            "Monitoring",
            "Playback"
    };
}
