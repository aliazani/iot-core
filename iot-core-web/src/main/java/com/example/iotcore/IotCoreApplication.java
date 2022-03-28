package com.example.iotcore;

import com.example.iotcore.paho.Subscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class IotCoreApplication {
    private final Subscriber subscriber;

    public static void main(String[] args) {
        log.info("########### Main Thread: " + Thread.currentThread().getName());
        SpringApplication.run(IotCoreApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> subscriber.run();
    }

}
