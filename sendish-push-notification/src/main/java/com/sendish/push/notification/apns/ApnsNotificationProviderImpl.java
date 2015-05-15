package com.sendish.push.notification.apns;

import com.notnoop.apns.APNS;
import com.notnoop.apns.PayloadBuilder;
import com.sendish.push.notification.JpaNotificationQueryHolder;
import com.sendish.push.notification.NotificationResult;
import com.sendish.repository.ApnsPushTokenRepository;
import com.sendish.repository.model.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ApnsNotificationProviderImpl implements ApnsNotificationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ApnsNotificationProviderImpl.class);

    private static final int TOKEN_MAX_PAGE_SIZE = 1000;

    private final Executor executor;

    @Autowired
    private ApnsPushTokenRepository apnsPushTokenRepository;

    @Autowired
    private ApnsNotificationSender apnsNotificationSender;

    public ApnsNotificationProviderImpl() {
        executor = Executors.newSingleThreadExecutor();
    }

    public ApnsNotificationProviderImpl(Executor executor) {
        this.executor = executor;
    }

    @Override
    public final NotificationResult pushNotificationMessage(final NotificationMessage notification, final String text, final Map<String, Object> customData, final JpaNotificationQueryHolder queryHolder) {
        Page<ApnsPushToken> pageToken = searchNext(queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            final String message = buildMessage(notification, text, customData);

            LOG.debug("Sending notifications message (type: {}, text: {}, refId: {}) --> total pages {}; total elements: {}", text,
                    notification.getType(),
                    notification.getReferenceId(),
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

            sendNotificationMessage(pageToken, notification, message);

            while (pageToken.hasNext()) {
                pageToken = searchNext(queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber() + 1, pageToken.getNumberOfElements());

                sendNotificationMessage(pageToken, notification, message);
            }
        }

        return new NotificationResult(NotificationPlatformType.APNS, pageToken.getTotalElements());
    }

    @Override
    public NotificationResult pushPlainTextMessage(String text, Map<String, Object> customData, JpaNotificationQueryHolder queryHolder) {
        Page<ApnsPushToken> pageToken = searchNext(queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            final String message = buildMessage(text, customData);

            LOG.debug("Sending plain text message (text: {}) --> total pages {}; total elements: {}", text,
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber() + 1, pageToken.getNumberOfElements());

            sendJsonMessage(pageToken.getContent(), message);

            while (pageToken.hasNext()) {
                pageToken = searchNext(queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

                sendJsonMessage(pageToken.getContent(), message);
            }
        }

        return new NotificationResult(NotificationPlatformType.APNS, pageToken.getTotalElements());
    }

    @Override
    public Map<String, Date> getInactiveDevicesDev() {
        return apnsNotificationSender.getDevInactiveDevices();
    }

    @Override
    public Map<String, Date> getInactiveDevices() {
        return apnsNotificationSender.getInactiveDevices();
    }

    public void sendMessage(ApnsPushToken token, String message) {
        String payload = buildMessage(message, null);

        if (token.isDevToken()) {
            apnsNotificationSender.sendDevPushMessage(payload, Collections.singletonList(token.getToken()));
        } else {
            sendPushMessage(payload, Collections.singletonList(token.getToken()));
        }
    }

    private String buildMessage(NotificationMessage notification, String text, Map<String, Object> data) {
        final PayloadBuilder builder = APNS.newPayload() //
                .alertBody(text) //
                .sound("default") //
                .customField("notificationType", notification.getType()) //
                .customField("referenceId", notification.getReferenceId());

        if (data != null) {
            for (Entry<String, Object> entry : data.entrySet()) {
                builder.customField(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    private String buildMessage(String text, Map<String, Object> data) {
        final PayloadBuilder builder = APNS.newPayload() //
                .alertBody(text) //
                .sound("default");

        if (data != null) {
            for (Entry<String, Object> entry : data.entrySet()) {
                builder.customField(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    private Page<ApnsPushToken> searchNext(JpaNotificationQueryHolder queryHolder, Pageable pageable) {
        return apnsPushTokenRepository.findAll(queryHolder.getApnsQuery(), pageable);
    }

    private void sendNotificationMessage(Page<ApnsPushToken> pageToken, NotificationMessage notification, final String payload) {
        final List<String> refIds =  pageToken.getContent().stream().filter(t -> !t.isDevToken()).map(PushToken::getToken).collect(Collectors.toList());
        if (!refIds.isEmpty()) {
            sendPushMessageAndCollectResults(payload, refIds, notification);
        }

        final List<String> devRefIds =  pageToken.getContent().stream().filter(PushToken::isDevToken).map(PushToken::getToken).collect(Collectors.toList());
        if (!devRefIds.isEmpty()) {
            apnsNotificationSender.sendDevPushMessageAndCollectResults(payload, refIds, notification.getId());
        }
    }

    private void sendJsonMessage(Collection<ApnsPushToken> tokens, String message) {
        final List<String> refIds =  tokens.stream().filter(t -> !t.isDevToken()).map(PushToken::getToken).collect(Collectors.toList());
        if (!refIds.isEmpty()) {
            sendPushMessage(message, refIds);
        }

        final List<String> devRefIds =  tokens.stream().filter(PushToken::isDevToken).map(PushToken::getToken).collect(Collectors.toList());
        if (!devRefIds.isEmpty()) {
            apnsNotificationSender.sendDevPushMessage(message, refIds);
        }
    }

    private void sendPushMessageAndCollectResults(String message, List<String> refIds, NotificationMessage notification) {
        executor.execute(() -> apnsNotificationSender.sendPushMessageAndCollectResults(message, refIds, notification.getId()));
    }

    private void sendPushMessage(String message, List<String> refIds) {
        executor.execute(() -> apnsNotificationSender.sendPushMessage(message, refIds));
    }

}
