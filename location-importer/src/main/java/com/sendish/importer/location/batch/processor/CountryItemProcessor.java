package com.sendish.importer.location.batch.processor;

import com.sendish.repository.CountryRepository;
import com.sendish.repository.model.jpa.Country;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CountryItemProcessor implements ItemProcessor<FieldSet, Country> {

    private CountryRepository countryRepository;

    @Autowired
    public CountryItemProcessor(CountryRepository countryRepository) {
        super();
        this.countryRepository = countryRepository;
    }

    @Override
    public Country process(FieldSet fieldSet) throws Exception {
        String iso = fieldSet.readString("iso");

        Country existingCountry = countryRepository.findByIso(iso);
        if (existingCountry == null) {
            String iso3 = fieldSet.readString("iso3");
            String name = fieldSet.readString("country");
            String currency = fieldSet.readString("currencyName");
            String currencyCode = fieldSet.readString("currencyCode");

            return new Country(name, iso, iso3, currency, currencyCode);
        } else {
            return null;
        }
    }

}
