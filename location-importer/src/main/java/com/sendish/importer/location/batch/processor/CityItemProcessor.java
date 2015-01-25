package com.sendish.importer.location.batch.processor;

import com.sendish.repository.CityRepository;
import com.sendish.repository.CountryRepository;
import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Country;
import com.sendish.repository.model.jpa.Location;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CityItemProcessor implements ItemProcessor<FieldSet, City> {

    private CityRepository cityRepository;
    private CountryRepository countryRepository;

    @Autowired
    public CityItemProcessor(CityRepository cityRepository, CountryRepository countryRepository) {
        super();
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public City process(FieldSet fieldSet) throws Exception {
        Integer externalId = fieldSet.readInt("geonameid");

        City existingCity = cityRepository.findByExternalId(externalId);
        if (existingCity == null) {
            City city = new City();
            city.setExternalId(externalId);
            city.setName(fieldSet.readString("name"));
            city.setTimezone(fieldSet.readString("timezone"));
            city.setPopulation(fieldSet.readInt("population"));
            city.setCountry(getCountry(fieldSet.readString("countryCode")));

            Location location = new Location(fieldSet.readBigDecimal("latitude"), fieldSet.readBigDecimal("longitude"));
            city.setLocation(location);

            return city;
        } else {
        	// TODO: Maybe update?
            return null;
        }
    }

    private Country getCountry(String countryCodeIso) {
        return countryRepository.findByIso(countryCodeIso);
    }

}
