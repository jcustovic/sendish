package com.sendish.api.util;

import org.springframework.util.StringUtils;

import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.User;

public class UserUtils {
	
	public static String getDisplayName(User user) {
		if (StringUtils.hasText(user.getNickname())) {
			return user.getNickname();
		} else {
			return CityUtils.getLocationName(user.getDetails().getCurrentCity());
		}
	}
	
	public static String getDisplayNameWithCity(User user) {
		City city = user.getDetails().getCurrentCity();
		if (StringUtils.hasText(user.getNickname())) {
			return user.getNickname() + ", " + city.getName();
		} else {
			return CityUtils.getLocationName(city);
		}
	}

}
