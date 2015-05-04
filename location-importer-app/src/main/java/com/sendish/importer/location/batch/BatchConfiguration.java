package com.sendish.importer.location.batch;

import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Country;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * http://download.geonames.org/export/dump/
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Bean
    public Job importCountriesJob(JobBuilderFactory jobs, Step countryStep1, Step cityStep1) {
        return jobs.get("importCountryJob")
                .incrementer(new RunIdIncrementer())
                .flow(countryStep1)
                .next(cityStep1)
                .end()
                .build();
    }

    /**
     * http://download.geonames.org/export/dump/countryInfo.txt
     *
     * @param stepBuilderFactory
     * @param countryItemReader
     * @param countryItemWriter
     * @param countryItemProcessor
     * @return
     */
    @Bean
    public Step countryStep1(StepBuilderFactory stepBuilderFactory, ItemReader<FieldSet> countryItemReader,
                      ItemWriter<Country> countryItemWriter, ItemProcessor<FieldSet, Country> countryItemProcessor) {
        return stepBuilderFactory.get("countryStep1")
                .<FieldSet, Country> chunk(10)
                .reader(countryItemReader)
                .processor(countryItemProcessor)
                .writer(countryItemWriter)
                .build();
    }

    /**
     * http://download.geonames.org/export/dump/cities5000.zip
     *
     * @param stepBuilderFactory
     * @param cityItemReader
     * @param cityItemWriter
     * @param cityItemProcessor
     * @return
     */
    @Bean
    public Step cityStep1(StepBuilderFactory stepBuilderFactory, ItemReader<FieldSet> cityItemReader,
                             ItemWriter<City> cityItemWriter, ItemProcessor<FieldSet, City> cityItemProcessor) {
        return stepBuilderFactory.get("cityStep1")
                .<FieldSet, City> chunk(500)
                // .faultTolerant().skipLimit(50).skip(Exception.class)
                .reader(cityItemReader)
                .processor(cityItemProcessor)
                .writer(cityItemWriter)
                .build();
    }

}
