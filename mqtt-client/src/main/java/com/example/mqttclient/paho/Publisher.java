package com.example.mqttclient.paho;

import com.example.mqttclient.paho.config.PahoConnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Publisher {
    public void run() {
        try {
            publish("topic", "message", 30);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message, int duration) throws MqttException {
        MqttClient client = new MqttClient(PahoConnectionProperties.PROTOCOL +
                PahoConnectionProperties.HOST + PahoConnectionProperties.PORT,
                PahoConnectionProperties.CLIENT_ID);

        MqttConnectionOptions connectionOptions = new MqttConnectionOptions();
        connectionOptions.setUserName(PahoConnectionProperties.USERNAME);
        connectionOptions.setPassword(PahoConnectionProperties.PASSWORD);
        connectionOptions.setCleanStart(false);
        connectionOptions.setMaxReconnectDelay(1000);
        client.connect(connectionOptions);

        // Required parameters for message publishing
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(PahoConnectionProperties.QOS);
        client.publish(topic, mqttMessage);

        int[] messageRates = new int[duration];
        for (int i = 0; i < duration; i++) {
            messageRates[i] = 0;
            for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(1); stop > System.nanoTime(); ) {
                client.publish(topic, mqttMessage);
                messageRates[i]++;
            }
        }

        client.disconnect();
        int sum = Arrays.stream(messageRates).sum();
        log.info(MessageFormat.format("All published messages: {0}", sum));
        log.info("published per Second: " + sum / duration);
        client.close();
    }
}