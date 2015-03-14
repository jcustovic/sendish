package com.sendish.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.UserSocialConnectionRepository;
import com.sendish.repository.model.jpa.UserSocialConnection;

@Service
@Transactional
public class UserSocialConnectionServiceImpl {
	
	@Autowired
	private UserSocialConnectionRepository userSocialConnectionRepository;

	public UserSocialConnection findByOAuth1(String providerId, String tokenValue, String tokenSecret) {
		return userSocialConnectionRepository.findByOAuth1(providerId, tokenValue, tokenSecret);
	}

	public UserSocialConnection findByOAuth2(String providerId, String accessToken, String refreshToken) {
		return userSocialConnectionRepository.findByOAuth2(providerId, accessToken, refreshToken);
	}

}
