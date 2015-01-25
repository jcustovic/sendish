package com.sendish.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties("app.redis.inmemory")
public class InMemoryRedisConfigurationProperties {

    private Resource executable;

    public void setExecutable(Resource executable) {
        this.executable = executable;
    }

    public Resource getExecutable() {
        return executable;
    }

}
