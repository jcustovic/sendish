package com.sendish.api.util;

import com.sendish.repository.model.jpa.City;
import org.apache.commons.lang3.StringUtils;

public class CityUtils {

    private static final int MAX_NAME_LENGTH = 24;

    public static final String getLocationName(City city) {
        String name = city.getName() + ", " + city.getCountry().getName();
        if (name.length() > MAX_NAME_LENGTH) {
            return StringUtils.left(name, MAX_NAME_LENGTH - 3) + "...";
        } else {
            return name;
        }
	}

}
