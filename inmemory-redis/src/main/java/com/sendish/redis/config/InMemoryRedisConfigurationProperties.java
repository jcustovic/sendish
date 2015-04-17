package com.sendish.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties("app.redis.inmemory")
public class InMemoryRedisConfigurationProperties {

    private Resource executable;
    private Integer port;

    public void setExecutable(Resource executable) {
        this.executable = executable;
    }

    public Resource getExecutable() {
        return executable;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

}
