package com.sendish.api.scheduler;

import com.sendish.api.notification.ApnsNotificationProvider;
import com.sendish.repository.ApnsPushTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class ApnsInvalidTokenChecker {

    private static final Logger LOG = LoggerFactory.getLogger(ApnsInvalidTokenChecker.class);

    private static final int FIVE_MIN_RATE = 300000;

    @Autowired
    private ApnsNotificationProvider apnsNotificationProvider;

    @Autowired
    private ApnsPushTokenRepository apnsPushTokenRepository;

    @Scheduled(fixedDelay = FIVE_MIN_RATE)
    public void checkForInvalidTokens() {
        Map<String, Date> inactiveDevices = apnsNotificationProvider.getInactiveDevices();
        LOG.debug("Invalid prod devices --> count: {}", inactiveDevices.size());
        invalidatedTokens(inactiveDevices);

        inactiveDevices = apnsNotificationProvider.getInactiveDevicesDev();
        LOG.debug("Invalid dev devices --> count: {}", inactiveDevices.size());
        invalidatedTokens(inactiveDevices);
    }

    private void invalidatedTokens(Map<String, Date> inactiveDevices) {
        if (!inactiveDevices.isEmpty()) {
            apnsPushTokenRepository.deleteByTokens(inactiveDevices.keySet());
        }
    }

}