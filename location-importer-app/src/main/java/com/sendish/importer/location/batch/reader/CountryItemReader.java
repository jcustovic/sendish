package com.sendish.importer.location.batch.reader;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CountryItemReader extends FlatFileItemReader<FieldSet> {

    public CountryItemReader() {
        super();
        setEncoding("UTF-8");
        setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(DelimitedLineTokenizer.DELIMITER_TAB) {{
                setComments(new String[] { "#" });
                setQuoteCharacter((char )0);
                setNames(new String[] { "iso", "iso3", "isoNumeric", "fips", "country", "capital", "area", "population"
                        , "continent", "tld", "currencyCode", "currencyName", "phone", "postalCodeFormat", "postalCodeRegex"
                        , "languages", "geonameId", "neighbours", "equivalentFipsCode"
                });
            }});
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
    }

    @Value("${sendish.batch.country.file.location}")
    @Override
    public void setResource(Resource resource) {
        super.setResource(resource);
    }

}
