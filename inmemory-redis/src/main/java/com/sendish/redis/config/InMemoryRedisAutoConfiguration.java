package com.sendish.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@ConditionalOnClass({ RedisServer.class })
@EnableConfigurationProperties({ InMemoryRedisConfigurationProperties.class })
@ConditionalOnProperty(prefix = "app.redis.inmemory", name = "enabled", matchIfMissing = true)
public class InMemoryRedisAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryRedisAutoConfiguration.class);

    @Autowired
    private InMemoryRedisConfigurationProperties config;

    @Bean
    public RedisServer redisServer() throws IOException {
        if (config.getExecutable() != null) {
            return new RedisServer(config.getExecutable().getFile(), config.getPort());
        }

        return RedisServer.builder()
        		.port(6379)
        		.setting("maxheap 2000000000") // Windows fix set heap to 2Gb
        		.build();
    }

    @PostConstruct
    public void start() throws IOException {
        logger.info("Starting redis server...");
        redisServer().start();
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
        logger.info("Stopping redis server...");
        redisServer().stop();
    }

}
