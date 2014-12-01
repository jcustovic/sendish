package com.sendish.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import com.sendish.repository.model.jpa.UserSocialConnection;

@Transactional(readOnly = true)
public interface UserSocialConnectionRepositoryCustom {

    List<UserSocialConnection> findConnectionsToUsers(Long p_userId, MultiValueMap<String, String> p_providerUsers);

    // TODO: Somehow cache this method since we cannot set @QueryHints. Maybe Spring @Cacheable?
    UserSocialConnection findByOAuth2(String providerId, String accessToken, String refreshToken);

    // TODO: Somehow cache this method since we cannot set @QueryHints Maybe Spring @Cacheable?
    UserSocialConnection findByOAuth1(String providerId, String tokenValue, String tokenSecret);

}
