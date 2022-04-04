package com.example.iotcore.mqttclient;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Callback implements MqttCallback {
    public static final int TEN_MESSAGE_FOR_EACH_TOPIC = 9;
    private final Map<String, Deque<String>> map = new HashMap<>();
    private WriteToFile writeToFile = new WriteToFile("messages-" + Instant.now() + ".csv");
    private long number = 0L;

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        log.info(MessageFormat.format("Disconnected because of: {0}", disconnectResponse.getReasonString()));
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        log.info(MessageFormat.format("Error: {0}", exception.getMessage()));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String content = number + " ," + Instant.now() + ", " + topic + ", " + message.toString() + "\n";

        map.computeIfPresent(topic, (key, value) -> {
            if (value.size() > TEN_MESSAGE_FOR_EACH_TOPIC)
                value.removeFirst();
            value.add(message.toString());

            return value;
        });

        if (number % 100_000 == 0 && number != 0)
            CompletableFuture.runAsync(() -> writeToFile = new WriteToFile("messages-" + Instant.now() + ".csv"));

        map.computeIfAbsent(topic, key -> {
            LinkedList<String> messageList = new LinkedList<>();
            messageList.add(message.toString());

            return messageList;
        });

        writeToFile.write(content);
        log.warn("Num: " + ++number);
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        log.info("*********** Delivery Complete ***********");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("*********** Connected ***********");
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        log.info(MessageFormat.format("options: {0}", properties.getReasonString()));
    }
}
