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
public class CityItemReader extends FlatFileItemReader<FieldSet> {

    public CityItemReader() {
        super();
        setEncoding("UTF-8");
        setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(DelimitedLineTokenizer.DELIMITER_TAB) {{
                setComments(new String[] { "#" });
                setQuoteCharacter((char )0);
                setNames(new String[] { "geonameid", "name", "asciiname", "alternatenames", "latitude", "longitude", "featureClass"
                        , "featureCode", "countryCode", "cc2", "admin1Code", "admin2Code", "admin3Code", "admin4Code", "population"
                        , "elevation", "dem", "timezone", "modificationDate"
                });
            }});
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
    }

    @Value("${sendish.batch.city.file.location}")
    @Override
    public void setResource(Resource resource) {
        super.setResource(resource);
    }

}
