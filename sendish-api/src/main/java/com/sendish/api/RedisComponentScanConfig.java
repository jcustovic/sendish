package com.sendish.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * If in memory redis is on classpath this will load it.
 * @see com.sendish.redis.config.InMemoryRedisAutoConfiguration
 */
@Configuration
@ComponentScan(basePackages = "com.sendish.redis")
public class RedisComponentScanConfig {
}
