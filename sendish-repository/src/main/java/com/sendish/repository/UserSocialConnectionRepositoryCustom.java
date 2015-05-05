package com.sendish.repository;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import com.sendish.repository.model.jpa.UserSocialConnection;

@Transactional(propagation = Propagation.MANDATORY)
public interface UserSocialConnectionRepositoryCustom {

    List<UserSocialConnection> findConnectionsToUsers(Long p_userId, MultiValueMap<String, String> p_providerUsers);

    UserSocialConnection findByOAuth2(String providerId, String accessToken, String refreshToken);

    UserSocialConnection findByOAuth1(String providerId, String tokenValue, String tokenSecret);

}
