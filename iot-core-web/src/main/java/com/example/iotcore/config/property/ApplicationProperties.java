package com.example.iotcore.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final CorsConfiguration cors = new CorsConfiguration();
    private JWT jwt = new JWT();
    private Email mail = new Email();
    private Ehcache ehcache = new Ehcache();

    @Getter
    @Setter
    public static class JWT {
        private String base64Secret;

        private String secret;

        private Long tokenValidityInSeconds;

        private Long tokenValidityInSecondsForRememberMe;

    }


    @Getter
    @Setter
    public static class Email {
        private String from;

        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Ehcache {
        private int timeToLiveSeconds;  // 1 hour
        private long maxEntries;
    }

}

