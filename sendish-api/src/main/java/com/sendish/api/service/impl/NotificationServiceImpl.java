package com.sendish.api.service.impl;

import com.sendish.repository.ApnsPushTokenRepository;
import com.sendish.repository.GcmPushTokenRepository;
import com.sendish.repository.NotificationMessageRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.GcmPushToken;
import com.sendish.repository.model.jpa.NotificationMessage;
import com.sendish.repository.model.jpa.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class NotificationServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

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

    public ApnsPushToken findApnsToken(String p_token, Long p_userId) {
        return apnsPushTokenRepository.findByTokenAndUserId(p_token, p_userId);
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

        try {
            apnsPushTokenRepository.save(token);
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn("Registering APNS push token {} failed for user {}", p_token, p_userId);
        }
    }

    private User getUser(Long p_userId) {
        return userRepository.findOne(p_userId);
    }

    public GcmPushToken findGcmToken(String p_token, Long p_userId) {
        return gcmPushTokenRepository.findByTokenAndUserId(p_token, p_userId);
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

        try {
            gcmPushTokenRepository.save(token);
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn("Registering GCM push token {} failed for user {}", p_token, p_userId);
        }
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
