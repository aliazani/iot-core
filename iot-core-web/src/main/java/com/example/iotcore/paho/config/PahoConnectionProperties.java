package com.example.mqttclient.paho.config;

import java.util.UUID;

public class PahoConnectionProperties {
    public static String PROTOCOL = "tcp://";
    public static String HOST = "localhost:";
    public static Integer PORT = 1884;

    public static String CLIENT_ID = String.valueOf(UUID.randomUUID());

    public static String USERNAME = "guest";
    public static byte[] PASSWORD = "guest".getBytes();

    public static int QOS = 0;
}
