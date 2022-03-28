package com.example.mqttclient.paho;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.time.Instant;

@Slf4j
public class Callback implements MqttCallback {
    //    private final Long messageCount = 0L;
    //    long start;
    RandomAccessFile stream;
    FileChannel channel;
    private int number = 0;
    private boolean startFlag = false;

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        log.info(MessageFormat.format("Disconnected because of: {0}", disconnectResponse.getReasonString()));
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        log.info(MessageFormat.format("Error: {0}", exception.getMessage()));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("**************** START ***************");
        String s = Instant.now() + " ," + topic + ", " + message + "\n";
        if (!startFlag) {
            stream = new RandomAccessFile("./test.text", "rw");
            channel = stream.getChannel();
            startFlag = true;
        }

        byte[] strBytes = s.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(strBytes.length);
        buffer.put(strBytes);
        buffer.flip();
        channel.write(buffer);
        number++;
        log.info("Count: " + number);


//        with open('myfile.dat', 'wb') as file:
//        b = bytearray(b'This is a sample')
//        file.write(b)
//
//        with open('myfile.dat', 'rb+') as file:
//        file.seek(5)
//        b1 = bytearray(b'  text')
//    #remember new bytes over write previous bytes
//        file.write(b1)
//
//        with open('myfile.dat', 'rb') as file:
//        print(file.read())

//        if (runningList.size() > 500 || (System.currentTimeMillis() - start) > 1000) {
//            log.info("############# Before save ....");
//            saveList = (CopyOnWriteArrayList<Message>) runningList.clone();
//            runningList.clear();
//            messageServiceSync.saveAll(saveList);
//            log.info("############ After save *********");
//            log.info("round: " + number++);
//        }
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
