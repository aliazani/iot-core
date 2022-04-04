package com.example.iotcore.mqttclient;

import com.example.iotcore.mqttclient.config.PahoConnectionProperties;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;


public class Subscriber {
    MqttClient client;

    public void run() {
        try {
            new Subscriber().subscribe();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe() throws MqttException {
        client = new MqttClient(PahoConnectionProperties.PROTOCOL +
                PahoConnectionProperties.HOST + PahoConnectionProperties.PORT,
                PahoConnectionProperties.CLIENT_ID, null);

        MqttConnectionOptions connectionOptions = new MqttConnectionOptions();
        connectionOptions.setCleanStart(false);
        connectionOptions.setMaxReconnectDelay(1000);
        connectionOptions.setUserName(PahoConnectionProperties.USERNAME);
        connectionOptions.setPassword(PahoConnectionProperties.PASSWORD.getBytes());
        client.connect(connectionOptions);
        client.setCallback(new Callback());
        client.subscribe(new MqttSubscription[]{new MqttSubscription("#", PahoConnectionProperties.QOS)});
    }
}
