package com.sendish.api.util;

import com.sendish.repository.model.jpa.City;

public class CityUtils {

    private static final int MAX_NAME_LENGTH = 24;

    public static final String getLocationName(City city) {
        return city.getName() + ", " + city.getCountry().getName();
    }

    public static final String getTrimmedLocationName(City city) {
        return getLocationName(city, MAX_NAME_LENGTH);
    }

    public static final String getLocationName(City city, int trimLength) {
    	String countryName = city.getCountry().getName();
    	trimLength -= countryName.length();

        return StringUtils.trim(city.getName(), --trimLength, "...") + ", " + countryName;
	}

}
