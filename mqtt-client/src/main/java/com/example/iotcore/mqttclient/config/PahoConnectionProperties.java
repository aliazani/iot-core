package com.example.iotcore.mqttclient.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PahoConnectionProperties {

    public static final String PROTOCOL = "tcp://";

    public static final String HOST = "localhost:";

    public static final Integer PORT = 1883;

    public static final String CLIENT_ID = String.valueOf(UUID.randomUUID());

    public static final String USERNAME = "guest";

    public static final String PASSWORD = "guest";

    public static final int QOS = 0;
}
