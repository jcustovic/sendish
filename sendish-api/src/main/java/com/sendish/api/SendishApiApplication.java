package com.sendish.api;

import com.sendish.repository.springframework.data.jpa.querydsl.CustomQueryDslJpaRepositoryFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = "com.sendish.repository.model.jpa")
@EnableJpaRepositories(repositoryFactoryBeanClass = CustomQueryDslJpaRepositoryFactoryBean.class, basePackages = "com.sendish.repository")
@EnableCaching
@EnableAsync
public class SendishApiApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendishApiApplication.class);

    public static void main(String args[]) {
        SpringApplication.run(SendishApiApplication.class, args);
        LOGGER.info("------------------ App started ------------------");
    }

}
