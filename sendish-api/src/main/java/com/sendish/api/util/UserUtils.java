package com.sendish.api.util;

import org.springframework.util.StringUtils;

import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.User;

public class UserUtils {

    private static final int MAX_NAME_LENGTH = 24;
	
	public static String getDisplayName(User user) {
        String displayName;
		if (StringUtils.hasText(user.getNickname())) {
			displayName = user.getNickname();
		} else {
			displayName = CityUtils.getLocationName(user.getDetails().getCurrentCity());
		}

        return com.sendish.api.util.StringUtils.trim(displayName, MAX_NAME_LENGTH);
	}

    public static String getDisplayNameWithCity(User user) {
		City city = user.getDetails().getCurrentCity();
        String displayName;
		if (StringUtils.hasText(user.getNickname())) {
            displayName = user.getNickname() + ", " + city.getName();
		} else {
            displayName = CityUtils.getLocationName(city);
		}

        return com.sendish.api.util.StringUtils.trim(displayName, MAX_NAME_LENGTH);
	}

}
