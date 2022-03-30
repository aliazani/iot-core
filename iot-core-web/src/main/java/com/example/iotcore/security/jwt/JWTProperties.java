package com.example.iotcore.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JWTProperties {
    private String base64Secret;

    private String secret;

    private Long tokenValidityInSeconds;

    private Long tokenValidityInSecondsForRememberMe;
}
