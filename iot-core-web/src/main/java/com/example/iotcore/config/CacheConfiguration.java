package com.example.iotcore.config;

import com.example.iotcore.config.property.ApplicationProperties;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {
    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(ApplicationProperties applicationProperties) {
        ApplicationProperties.Ehcache ehcache = applicationProperties.getEhcache();

        jcacheConfiguration =
                Eh107Configuration.fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                                .build()
                );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, com.example.iotcore.security.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, com.example.iotcore.security.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, com.example.iotcore.security.domain.User.class.getName());
            createCache(cm, com.example.iotcore.security.domain.Authority.class.getName());
            createCache(cm, com.example.iotcore.security.domain.User.class.getName() + ".authorities");
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) cache.clear();
        else cm.createCache(cacheName, jcacheConfiguration);
    }
}
