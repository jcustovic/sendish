package com.sendish.api.util;

import org.springframework.util.StringUtils;

import com.sendish.repository.model.jpa.User;

public class UserUtils {
	
	public static String getDisplayName(User user) {
		if (StringUtils.hasText(user.getNickname())) {
			return user.getNickname();
		} else {
			return CityUtils.getLocationName(user.getDetails().getCurrentCity());
		}
	}

}
