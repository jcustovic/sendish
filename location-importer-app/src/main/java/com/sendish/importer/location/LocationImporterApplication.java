package com.sendish.importer.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sendish.repository.CityRepository;
import com.sendish.repository.CountryRepository;
import com.sendish.repository.springframework.data.jpa.querydsl.CustomQueryDslJpaRepositoryFactoryBean;

@SpringBootApplication
@EntityScan(basePackages = "com.sendish.repository.model.jpa")
@EnableJpaRepositories(repositoryFactoryBeanClass = CustomQueryDslJpaRepositoryFactoryBean.class, basePackages = "com.sendish.repository")
@EnableTransactionManagement
public class LocationImporterApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationImporterApplication.class);

    public static void main(String args[]) {
        ConfigurableApplicationContext context = SpringApplication.run(LocationImporterApplication.class, args);

        LOGGER.info("------------------ RESULT ------------------");
        CountryRepository countryRepository = context.getBean(CountryRepository.class);
        LOGGER.info("Total countries : {}", countryRepository.count());

        CityRepository cityRepository = context.getBean(CityRepository.class);
        LOGGER.info("Total cities : {}", cityRepository.count());
    }

}
