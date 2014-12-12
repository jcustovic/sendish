package com.sendish.redis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@ConditionalOnClass({ RedisServer.class })
public class InMemoryRedisAutoConfiguration {

    @Bean
    public RedisServer redisServer() throws IOException {
        RedisServer redisServer = RedisServer.builder()
                .port(6379)
                .build();

        return redisServer;
    }

    @PostConstruct
    public void start() throws IOException {
        redisServer().start();
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
        redisServer().stop();
    }

}
