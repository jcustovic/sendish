package com.sendish.batch;

import com.sendish.repository.springframework.data.jpa.querydsl.CustomQueryDslJpaRepositoryFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = "com.sendish.repository.model.jpa")
@EnableJpaRepositories(repositoryFactoryBeanClass = CustomQueryDslJpaRepositoryFactoryBean.class, basePackages = "com.sendish.repository")
public class BatchApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchApplication.class);

    public static void main(String args[]) {
        SpringApplication.run(BatchApplication.class, args);
        LOGGER.info("------------------ App started ------------------");
    }

}
