package com.sendish.api.notification;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import com.sendish.repository.ApnsPushTokenRepository;
import com.sendish.repository.NotificationPartialResultRepository;
import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.NotificationMessage;
import com.sendish.repository.model.jpa.NotificationPartialResult;
import com.sendish.repository.model.jpa.NotificationPlatformType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

//@Service("apnsNotificationProvider")
public class ApnsNotificationProvider implements NotificationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ApnsNotificationProvider.class);

    private static final int TOKEN_MAX_PAGE_SIZE = 1000;

    private static final int EXPIRY_MINUTES = 60;

    private static final int POOL_SIZE = 10;

    @Autowired
    private transient ApnsPushTokenRepository apnsPushTokenRepository;

    @Autowired
    private transient NotificationPartialResultRepository notificationPartialResultRepository;

    @Value("${apns.cert.path}")
    private transient File certificatePath;

    @Value("${apns.cert.password}")
    private transient String certificatePassword;

    @Value("${apns.cert.dev.path}")
    private transient File devCertificatePath;

    @Value("${apns.cert.dev.password}")
    private transient String devCertificatePassword;

    @Qualifier("notificationExecutor")
    @Autowired
    private transient TaskExecutor taskExecutor;

    @Autowired
    private transient ErrorApnsDelegate apnsDelegate;

    private transient ApnsService prodService;
    private transient ApnsService devService;

    @PostConstruct
    public void setup() {
        prodService = new JKSApnsServiceBuilder() //
                .withCert(certificatePath.getAbsolutePath(), certificatePassword) //
                .withProductionDestination() //
                .asPool(POOL_SIZE) //
                .withDelegate(apnsDelegate) //
                .build();

        devService = new JKSApnsServiceBuilder() //
                .withCert(devCertificatePath.getAbsolutePath(), certificatePassword) //
                .withSandboxDestination() //
                .withDelegate(apnsDelegate) //
                .build();
    }

    @Transactional
    @Override
    public final NotificationResult notifyByToken(final NotificationMessage p_notification, final String p_text, final Map<String, Object> p_data, final String p_token, final boolean p_devToken) {
        LOG.debug("Sending notification by user token (type: {}, text: {}, refId: {}, token: {}) --> msg {}", p_notification.getType(),
                p_text,
                p_notification.getReferenceId(),
                p_token,
                p_text);
        final String message = buildMessage(p_notification, p_text, p_data);

        if (p_devToken) {
            sendMsg(devService, message, Arrays.asList(p_token), p_notification);
        } else {
            sendMsg(prodService, message, Arrays.asList(p_token), p_notification);
        }

        return new NotificationResult(NotificationPlatformType.APNS, 1L);
    }

    @Transactional
    @Override
    public final NotificationResult notify(final NotificationMessage p_notification, final String p_text, final Map<String, Object> p_data, final JpaNotificationQueryHolder p_queryHolder) {
        Page<ApnsPushToken> pageToken = searchNext(p_queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            final String message = buildMessage(p_notification, p_text, p_data);

            LOG.debug("Sending notifications (type: {}, text: {}, refId: {}) --> total pages {}; total elements: {}", p_text,
                    p_notification.getType(),
                    p_notification.getReferenceId(),
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

            saveAndSend(pageToken, p_notification, message);

            while (pageToken.hasNext()) {
                pageToken = searchNext(p_queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

                saveAndSend(pageToken, p_notification, message);
            }
        }

        return new NotificationResult(NotificationPlatformType.APNS, pageToken.getTotalElements());
    }

    private String buildMessage(NotificationMessage p_notification, String p_text, Map<String, Object> p_data) {
        final PayloadBuilder builder = APNS.newPayload() //
                .alertBody(p_text) //
                .sound("default") //
                .customField("notificationType", p_notification.getType());

        if (p_notification.getReferenceId() != null) {
            builder.customField("referenceId", p_notification.getReferenceId().toString());
        }
        if (p_data != null) {
            for (Entry<String, Object> entry : p_data.entrySet()) {
                builder.customField(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    private Page<ApnsPushToken> searchNext(final JpaNotificationQueryHolder p_queryHolder, final Pageable p_pageable) {
        return apnsPushTokenRepository.findAll(p_queryHolder.getApnsQuery(), p_pageable);
    }

    private void saveAndSend(final Page<ApnsPushToken> p_pageToken, final NotificationMessage p_notification, final String p_payload) {
        final List<String> refIds = new ArrayList<>(p_pageToken.getNumberOfElements());
        final List<String> devRefIds = new LinkedList<>();
        for (final ApnsPushToken token : p_pageToken) {
            if (token.isDevToken()) {
                LOG.debug("Sending to test device with token {}", token.getToken());
                devRefIds.add(token.getToken());
            } else {
                refIds.add(token.getToken());
            }
        }

        if (!refIds.isEmpty()) {
            sendMsg(prodService, p_payload, refIds, p_notification);
        }
        if (!devRefIds.isEmpty()) {
            sendMsg(devService, p_payload, devRefIds, p_notification);
        }
    }

    private void sendMsg(final ApnsService p_service, final String p_message, final List<String> p_refIds, final NotificationMessage p_notification) {
        final long expiry = DateTime.now().plusMinutes(EXPIRY_MINUTES).getMillis(); // Expire in 60min
        taskExecutor.execute(() -> {
            final NotificationPartialResult notificationResult = new NotificationPartialResult();
            notificationResult.setNotification(p_notification);
            notificationResult.setPlatformType(NotificationPlatformType.APNS);
            notificationResult.setSendDate(new Date());
            notificationResult.setTotalCount(p_refIds.size());

            // Send notification
            p_service.push(p_refIds, p_message, new Date(expiry));

            notificationResult.setResponseDate(new Date());
            final Map<String, Date> inactiveDevices = p_service.getInactiveDevices();
            notificationResult.setFailureCount(inactiveDevices.size());
            notificationPartialResultRepository.save(notificationResult);

            LOG.debug("Notification sent --> total: {}, inactive: {}", new Object[]{p_refIds.size(), inactiveDevices.size()});
            if (!inactiveDevices.isEmpty()) {
                LOG.info("Deleting inactive devices...");
                apnsPushTokenRepository.deleteByTokens(inactiveDevices.keySet());
            }
        });
    }

}
