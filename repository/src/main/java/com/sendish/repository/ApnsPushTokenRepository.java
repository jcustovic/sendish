package com.sendish.repository;

import com.sendish.repository.model.jpa.ApnsPushToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Transactional(readOnly = true)
public interface ApnsPushTokenRepository extends PushTokenRepository<ApnsPushToken> {

    ApnsPushToken findByToken(String p_token);

    ApnsPushToken findByTokenAndUserId(String p_token, Long p_userId);

    @Modifying
    @Transactional
    @Query("DELETE ApnsPushToken apns WHERE apns.token IN ?1")
    void deleteByTokens(Collection<String> p_tokens);

    @Modifying
    @Transactional
    @Query("DELETE ApnsPushToken apns WHERE apns.token = ?1")
    void deleteByToken(String p_token);

}
