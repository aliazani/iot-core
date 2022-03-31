package com.example.iotcore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class WriteToFile {
//    BufferedWriter bufferedWriter;
    Path path;

    public WriteToFile(String path) {
        this.path = Paths.get(path);
//        try {
//            bufferedWriter = new BufferedWriter(new FileWriter(fileName));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public void write(String content) {
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

    public void close() {
//        try {
//            bufferedWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
