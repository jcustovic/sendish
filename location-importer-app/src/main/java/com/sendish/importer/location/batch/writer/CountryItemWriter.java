package com.sendish.importer.location.batch.writer;

import com.sendish.repository.CountryRepository;
import com.sendish.repository.model.jpa.Country;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CountryItemWriter implements ItemWriter<Country> {

    private CountryRepository countryRepository;

    @Autowired
    public CountryItemWriter(CountryRepository countryRepository) {
        super();
        this.countryRepository = countryRepository;
    }

    @Override
    public void write(List<? extends Country> items) throws Exception {
        countryRepository.save(items);
    }

}
