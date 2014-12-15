package com.sendish.api.service.impl;

import com.sendish.repository.ApnsPushTokenRepository;
import com.sendish.repository.GcmPushTokenRepository;
import com.sendish.repository.NotificationMessageRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.GcmPushToken;
import com.sendish.repository.model.jpa.NotificationMessage;
import com.sendish.repository.model.jpa.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class NotificationServiceImpl {

    @Autowired
    private ApnsPushTokenRepository apnsPushTokenRepository;

    @Autowired
    private GcmPushTokenRepository gcmPushTokenRepository;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public NotificationMessage save(NotificationMessage p_notification) {
        return notificationMessageRepository.save(p_notification);
    }

    public void registerApns(String p_token, Long p_userId, boolean p_devToken) {
        ApnsPushToken token = apnsPushTokenRepository.findByToken(p_token);

        if (token == null) {
            token = new ApnsPushToken(p_token, getUser(p_userId));
        } else {
            token.setUser(getUser(p_userId));
            // Force update of modify date in case customer has not changed
            token.setModifiedDate(new Date());
        }
        token.setDevToken(p_devToken);

        apnsPushTokenRepository.save(token);
    }

    private User getUser(Long p_userId) {
        return userRepository.findOne(p_userId);
    }

    public void registerGcm(String p_token, Long p_userId) {
        GcmPushToken token = gcmPushTokenRepository.findByToken(p_token);

        if (token == null) {
            token = new GcmPushToken(p_token, getUser(p_userId));
        } else {
            token.setUser(getUser(p_userId));
            // Force update of modify date in case customer has not changed
            token.setModifiedDate(new Date());
        }

        gcmPushTokenRepository.save(token);
    }

    public void unregisterApns(String p_token, Long p_userId) {
        ApnsPushToken token = apnsPushTokenRepository.findByTokenAndUserId(p_token, p_userId);
        if (token == null) {
            throw new RuntimeException("Token " + p_token + " not found for user " + p_userId);
        }

        apnsPushTokenRepository.delete(token);
    }

    public void unregisterGcm(String p_token, Long p_userId) {
        GcmPushToken token = gcmPushTokenRepository.findByTokenAndUserId(p_token, p_userId);
        if (token == null) {
            throw new RuntimeException("Token " + p_token + " not found for user " + p_userId);
        }

        gcmPushTokenRepository.delete(token);
    }

}
