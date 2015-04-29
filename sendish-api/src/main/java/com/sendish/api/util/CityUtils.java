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
        int cityLength = city.getName().length();
        if (cityLength > trimLength - 5) {
            return StringUtils.trim(getLocationName(city), trimLength, "...,"); // Because of iOS, we have to have ','
        } else if (cityLength == trimLength - 5) {
            return city.getName() + ", " + city.getCountry().getIso();
        //} else if (cityLength > trimLength - 8) {
        //    return city.getName() + ", " + city.getCountry().getIso3();
        } else {
            return StringUtils.trim(getLocationName(city), trimLength, "...");
        }
	}

}
