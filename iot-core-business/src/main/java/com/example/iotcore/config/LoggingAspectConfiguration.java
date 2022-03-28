package com.example.iotcore.config;

import com.example.iotcore.aop.logging.LoggingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile("dev")
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}
