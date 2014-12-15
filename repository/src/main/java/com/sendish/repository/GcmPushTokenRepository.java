package com.sendish.repository;

import com.sendish.repository.model.jpa.GcmPushToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Transactional(readOnly = true)
public interface GcmPushTokenRepository extends PushTokenRepository<GcmPushToken> {

    GcmPushToken findByToken(String p_token);

    GcmPushToken findByTokenAndUserId(String p_token, Long p_user);

    @Modifying
    @Transactional
    @Query("DELETE GcmPushToken gcm WHERE gcm.token IN ?1")
    void deleteByTokens(Collection<String> p_tokens);

    @Modifying
    @Transactional
    @Query("DELETE GcmPushToken gcm WHERE gcm.token = ?1")
    void deleteByToken(String p_token);

    @Modifying
    @Transactional
    @Query("UPDATE GcmPushToken gcm SET gcm.token = ?2 WHERE gcm.token = ?1")
    void updateToken(String p_oldToken, String p_newToken);

}
