package com.sendish.repository;

import com.sendish.repository.model.jpa.ApnsPushToken;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Stream;

@Transactional(propagation = Propagation.MANDATORY)
public interface ApnsPushTokenRepository extends PushTokenRepository<ApnsPushToken> {

    ApnsPushToken findByToken(String p_token);

    ApnsPushToken findByTokenAndUserId(String p_token, Long p_userId);

    @Modifying
    @Transactional
    @Query("DELETE ApnsPushToken apt WHERE apt.token IN ?1")
    void deleteByTokens(Collection<String> p_tokens);

    @Modifying
    @Transactional
    @Query("DELETE ApnsPushToken apt WHERE apt.token = ?1")
    void deleteByToken(String p_token);

    @Query("SELECT apt FROM ApnsPushToken apt WHERE apt.user.details.lastInteractionTime < ?1 AND apt.user.details.comeBackReminderTime IS NULL")
    Stream<ApnsPushToken> streamUsersThatRequiredFirstComeBackMsgToBeSent(DateTime lastInteractionLower);

    @Query("SELECT apt FROM ApnsPushToken apt WHERE apt.user.details.lastInteractionTime < ?1 AND apt.user.details.comeBackReminderTime < ?2")
    Stream<ApnsPushToken> streamUsersThatRequiredRecurringComeBackMsgToBeSent(DateTime lastInteractionLower, DateTime lastReminderTimeSentLower);

}
