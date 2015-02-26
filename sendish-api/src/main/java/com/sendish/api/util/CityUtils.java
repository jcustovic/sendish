package com.sendish.api.util;

import com.sendish.repository.model.jpa.City;

public class CityUtils {

	public static final String getLocationName(City city) {
		return city.getName() + ", " + city.getCountry().getName();
	}

}
