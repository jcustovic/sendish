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
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ApnsNotificationProvider implements NotificationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ApnsNotificationProvider.class);

    private static final int TOKEN_MAX_PAGE_SIZE = 1000;

    private static final int ONE_HOUR_EXPIRY = 60;
    private static final int TWELVE_HOUR_EXPIRY = 60;

    private static final int POOL_SIZE = 10;

    public static final int ONE_HOUR_RATE = 3600000;

    @Autowired
    private ApnsPushTokenRepository apnsPushTokenRepository;

    @Autowired
    private NotificationPartialResultRepository notificationPartialResultRepository;

    @Value("${app.ios.cert.path:}")
    private Resource certificatePath;

    @Value("${app.ios.cert.pass:}")
    private String certificatePassword;

    @Value("${app.ios.cert.dev.path:}")
    private Resource devCertificatePath;

    @Value("${app.ios.cert.dev.pass:}")
    private String devCertificatePassword;

    @Qualifier("apnsNotificationExecutor")
    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private transient ErrorApnsDelegate apnsDelegate;

    private transient ApnsService prodService;
    private transient ApnsService devService;

    @PostConstruct
    public void setup() throws IOException {
        if (certificatePath != null && certificatePath.exists()) {
            prodService = new JKSApnsServiceBuilder() //
                    .withCert(certificatePath.getInputStream(), certificatePassword) //
                    .withProductionDestination() //
                    .asPool(POOL_SIZE) //
                    .withDelegate(apnsDelegate) //
                    .build();
        }

        if (devCertificatePath != null && devCertificatePath.exists()) {
            devService = new JKSApnsServiceBuilder() //
                    .withCert(devCertificatePath.getInputStream(), devCertificatePassword) //
                    .withSandboxDestination() //
                    .withDelegate(apnsDelegate) //
                    .build();
        }
    }

    // TODO: Move to separate class
    // @Scheduled(fixedDelay = ONE_HOUR_RATE)
    public void checkForInvalidTokens() {
        if (prodService != null) {
            Map<String, Date> inactiveDevices = prodService.getInactiveDevices();
            LOG.info("Invalid prod devices --> count: {}", inactiveDevices.size());
            invalidatedTokens(inactiveDevices);
        }

        if (devService != null) {
            Map<String, Date> inactiveDevices = devService.getInactiveDevices();
            LOG.info("Invalid prod devices --> count: {}", inactiveDevices.size());
            invalidatedTokens(inactiveDevices);
        }
    }

    @Transactional
    @Override
    public final NotificationResult pushNotificationMessage(final NotificationMessage p_notification, final String p_text, final Map<String, Object> p_customData, final JpaNotificationQueryHolder p_queryHolder) {
        Page<ApnsPushToken> pageToken = searchNext(p_queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            final String message = buildMessage(p_notification, p_text, p_customData);

            LOG.debug("Sending notifications message (type: {}, text: {}, refId: {}) --> total pages {}; total elements: {}", p_text,
                    p_notification.getType(),
                    p_notification.getReferenceId(),
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

            sendMessageNotificationMessage(pageToken, p_notification, message);

            while (pageToken.hasNext()) {
                pageToken = searchNext(p_queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

                sendMessageNotificationMessage(pageToken, p_notification, message);
            }
        }

        return new NotificationResult(NotificationPlatformType.APNS, pageToken.getTotalElements());
    }

    @Override
    public NotificationResult pushPlainTextMessage(String p_text, Map<String, Object> p_customData, JpaNotificationQueryHolder p_queryHolder) {
        Page<ApnsPushToken> pageToken = searchNext(p_queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            final String message = buildMessage(p_text, p_customData);

            LOG.debug("Sending plain text message (text: {}) --> total pages {}; total elements: {}", p_text,
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

            sendMessage(pageToken, message);

            while (pageToken.hasNext()) {
                pageToken = searchNext(p_queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

                sendMessage(pageToken, message);
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

    private String buildMessage(String p_text, Map<String, Object> p_data) {
        final PayloadBuilder builder = APNS.newPayload() //
                .alertBody(p_text) //
                .sound("default");

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

    private void sendMessageNotificationMessage(final Page<ApnsPushToken> p_pageToken, final NotificationMessage p_notification, final String p_payload) {
        final List<String> refIds =  p_pageToken.getContent().stream().filter(t -> !t.isDevToken()).map(t -> t.getToken()).collect(Collectors.toList());
        if (!refIds.isEmpty()) {
            sendPushMessageAndCollectResults(prodService, p_payload, refIds, p_notification);
        }

        final List<String> devRefIds =  p_pageToken.getContent().stream().filter(t -> t.isDevToken()).map(t -> t.getToken()).collect(Collectors.toList());
        if (!devRefIds.isEmpty()) {
            sendPushMessageAndCollectResults(devService, p_payload, devRefIds, p_notification);
        }
    }


    private void sendMessage(Page<ApnsPushToken> p_pageToken, String p_message) {
        final List<String> refIds =  p_pageToken.getContent().stream().filter(t -> !t.isDevToken()).map(t -> t.getToken()).collect(Collectors.toList());
        if (!refIds.isEmpty()) {
            sendPushMessage(prodService, p_message, refIds);
        }

        final List<String> devRefIds =  p_pageToken.getContent().stream().filter(t -> t.isDevToken()).map(t -> t.getToken()).collect(Collectors.toList());
        if (!devRefIds.isEmpty()) {
            sendPushMessage(devService, p_message, devRefIds);
        }
    }

    private void sendPushMessageAndCollectResults(final ApnsService p_service, final String p_message, final List<String> p_refIds, final NotificationMessage p_notification) {
        final long expiry = DateTime.now().plusMinutes(TWELVE_HOUR_EXPIRY).getMillis(); // Expire in 60min
        taskExecutor.execute(() -> {
            final NotificationPartialResult notificationResult = new NotificationPartialResult();
            notificationResult.setNotification(p_notification);
            notificationResult.setPlatformType(NotificationPlatformType.APNS);
            notificationResult.setSendDate(DateTime.now());
            notificationResult.setTotalCount(p_refIds.size());

            // Send notification
            p_service.push(p_refIds, p_message, new Date(expiry));

            notificationResult.setResponseDate(DateTime.now());
            notificationResult.setFailureCount(0); // We cannot know the failure count
            notificationPartialResultRepository.save(notificationResult);

            LOG.debug("Notification sent --> count: {}", p_refIds.size());
        });
    }

    private void sendPushMessage(final ApnsService p_service, final String p_message, final List<String> p_refIds) {
        final long expiry = DateTime.now().plusMinutes(ONE_HOUR_EXPIRY).getMillis(); // Expire in 60min
        taskExecutor.execute(() -> {
            p_service.push(p_refIds, p_message, new Date(expiry));
        });
    }

    private void invalidatedTokens(Map<String, Date> inactiveDevices) {
        if (!inactiveDevices.isEmpty()) {
            LOG.debug("Deleting inactive devices...");
            apnsPushTokenRepository.deleteByTokens(inactiveDevices.keySet());
        }
    }

}
