package com.example.mqttclient.paho;

import com.example.mqttclient.paho.config.PahoConnectionProperties;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.*;


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
                PahoConnectionProperties.CLIENT_ID);

        MqttConnectionOptions connectionOptions = new MqttConnectionOptions();
        connectionOptions.setCleanStart(false);
        connectionOptions.setMaxReconnectDelay(1000);
        connectionOptions.setUserName(PahoConnectionProperties.USERNAME);
        connectionOptions.setPassword(PahoConnectionProperties.PASSWORD);
        client.connect(connectionOptions);
        client.setCallback(new Callback());
        client.subscribe(new MqttSubscription[]{new MqttSubscription("#", PahoConnectionProperties.QOS)});
    }
}
