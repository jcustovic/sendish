package com.sendish.push.notification.gcm;

import com.google.android.gcm.server.*;
import com.sendish.push.notification.JpaNotificationQueryHolder;
import com.sendish.push.notification.NotificationProvider;
import com.sendish.push.notification.NotificationResult;
import com.sendish.repository.GcmPushTokenRepository;
import com.sendish.repository.model.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GcmNotificationProviderImpl implements NotificationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GcmNotificationProviderImpl.class);

    private static final int TOKEN_MAX_PAGE_SIZE = 1000;

    private static final int TTL_SECONDS         = 43200; // 12 hours

    private final Executor executor;

    @Autowired
    private GcmPushTokenRepository gcmPushTokenRepository;

    @Autowired
    private GcmNotificationSender gcmNotificationSender;

    public GcmNotificationProviderImpl() {
        executor = Executors.newSingleThreadExecutor();
    }

    public GcmNotificationProviderImpl(Executor executor) {
        this.executor = executor;
    }

    @Override
    public NotificationResult pushNotificationMessage(NotificationMessage notification, String text, Map<String, Object> customData, JpaNotificationQueryHolder queryHolder) {
        Page<GcmPushToken> pageToken = searchNext(queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            Message message = buildMessage(text, customData);

            LOG.debug("Sending notifications message (type: {}, text: {}, refId: {}) --> total pages {}; total elements: {}", text,
                    notification.getType(),
                    notification.getReferenceId(),
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

            List<String> refIds = getRefIds(pageToken.getContent());
            sendNotificationMessage(refIds, message, notification);

            while (pageToken.hasNext()) {
                pageToken = searchNext(queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber() + 1, pageToken.getNumberOfElements());

                refIds = getRefIds(pageToken.getContent());
                sendNotificationMessage(refIds, message, notification);
            }
        }

        return new NotificationResult(NotificationPlatformType.APNS, pageToken.getTotalElements());
    }

    @Override
    public NotificationResult pushPlainTextMessage(String text, Map<String, Object> customData, JpaNotificationQueryHolder queryHolder) {
        Page<GcmPushToken> pageToken = searchNext(queryHolder, new PageRequest(0, TOKEN_MAX_PAGE_SIZE));
        if (pageToken.getNumberOfElements() > 0) {
            Message message = buildMessage(text, customData);

            LOG.debug("Sending plain text message (text: {}) --> total pages {}; total elements: {}", text,
                    pageToken.getTotalPages(),
                    pageToken.getTotalElements());
            LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber() + 1, pageToken.getNumberOfElements());

            List<String> refIds = getRefIds(pageToken.getContent());
            sendNotificationMessage(refIds, message);

            while (pageToken.hasNext()) {
                pageToken = searchNext(queryHolder, pageToken.nextPageable());

                LOG.debug("\t --> sending page {} with {} tokens", pageToken.getNumber(), pageToken.getNumberOfElements());

                refIds = getRefIds(pageToken.getContent());
                sendNotificationMessage(refIds, message);
            }
        }

        return new NotificationResult(NotificationPlatformType.APNS, pageToken.getTotalElements());
    }

    private List<String> getRefIds(List<GcmPushToken> tokens) {
        return tokens.stream().map(GcmPushToken::getToken).collect(Collectors.toList());
    }

    private Message buildMessage(String text, Map<String, Object> customData) {
        Message.Builder messageBuilder = new Message.Builder() //
                .collapseKey("1") //
                .timeToLive(TTL_SECONDS) //
                .delayWhileIdle(false) //
                .addData("message", text);

        if (customData != null) {
            for (Map.Entry<String, Object> entry : customData.entrySet()) {
                messageBuilder.addData(entry.getKey(), entry.getValue().toString());
            }
        }

        return messageBuilder.build();
    }

    private Page<GcmPushToken> searchNext(final JpaNotificationQueryHolder queryHolder, final Pageable pageable) {
        return gcmPushTokenRepository.findAll(queryHolder.getGcmQuery(), pageable);
    }

    private void sendNotificationMessage(List<String> refIds, Message message, NotificationMessage notification) {
        executor.execute(() -> {
            try {
                gcmNotificationSender.sendNotificationMessage(refIds, message, notification.getId());
            } catch (final IOException e) {
                LOG.error("Error sending gcm notification", e);
            }
        });
    }

    private void sendNotificationMessage(List<String> refIds, Message message) {
        executor.execute(() -> {
            try {
                gcmNotificationSender.sendPushMessage(refIds, message);
            } catch (final IOException e) {
                LOG.error("Error sending gcm notification", e);
            }
        });
    }

}
