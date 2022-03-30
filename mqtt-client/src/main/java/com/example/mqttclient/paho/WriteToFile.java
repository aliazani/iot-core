package com.example.mqttclient.paho;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class WriteToFile {
//    BufferedWriter bufferedWriter;
    Path path;

    public WriteToFile(String fileName) {
        path = Paths.get(fileName);
//        try {
//            bufferedWriter = new BufferedWriter(new FileWriter(fileName));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public void writeToFile(String content) {
        try {
            Files.write(path, content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            bufferedWriter.append(content);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void closeFile() {
//        try {
//            bufferedWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
