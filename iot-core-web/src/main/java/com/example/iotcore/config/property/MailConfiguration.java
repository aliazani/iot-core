package com.example.iotcore.config.property;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class MailConfiguration {

    @Bean
    public JavaMailSender mailSender() {
        return  new JavaMailSenderImpl();
    }
}
