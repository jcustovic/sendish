package com.sendish.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EntityScan(basePackages = "com.sendish.repository.model.jpa")
@EnableJpaRepositories(basePackages = "com.sendish.repository")
@EnableTransactionManagement
@EnableConfigurationProperties
@EnableCaching
public class SendishApiApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendishApiApplication.class);

    public static void main(String args[]) {
        ConfigurableApplicationContext context = SpringApplication.run(SendishApiApplication.class, args);

        LOGGER.info("------------------ App started ------------------");
    }

}
