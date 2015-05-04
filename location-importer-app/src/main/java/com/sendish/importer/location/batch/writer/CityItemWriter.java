package com.sendish.importer.location.batch.writer;

import com.sendish.repository.CityRepository;
import com.sendish.repository.model.jpa.City;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CityItemWriter implements ItemWriter<City> {

    private CityRepository cityRepository;

    @Autowired
    public CityItemWriter(CityRepository cityRepository) {
        super();
        this.cityRepository = cityRepository;
    }

    @Override
    public void write(List<? extends City> items) throws Exception {
        cityRepository.save(items);
    }

}
