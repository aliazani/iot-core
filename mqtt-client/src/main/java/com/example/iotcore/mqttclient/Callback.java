package com.example.iotcore;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.text.MessageFormat;
import java.time.Instant;

@Slf4j
public class Callback implements MqttCallback {
    private final WriteToFile writeToFile = new WriteToFile("messages-" + Instant.now() + ".csv");
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
        String content = ++number + ", " + Instant.now() + ", " + topic + ", " + message.toString() + "\n";
        writeToFile.write(content);
        System.out.println("Num: " + number);
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
