package com.sendish.api.notification;

import com.sendish.repository.NotificationMessageRepository;
import com.sendish.repository.model.jpa.NotificationMessage;
import com.sendish.repository.model.jpa.NotificationPlatformType;
import com.sendish.repository.model.jpa.NotificationStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.*;

public class DelegateNotificationProvider implements AsyncNotificationProvider {

    private static final Logger              LOG = LoggerFactory.getLogger(DelegateNotificationProvider.class);

    @Autowired(required = false)
    private List<NotificationProvider>       notifyProviders;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    @Async
    @Override
    public final void sendPlainTextNotification(final String p_message, Long p_userId) {
        try {
            notify(p_message, null, new UserQueryHolder(p_userId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Async
    @Override
    public final void sendNotificationMessage(final Long p_refId, final String p_refType, final String p_message, final Map<String, Object> p_customData, Long p_userId) {
        try {
            NotificationMessage notification = new NotificationMessage();
            notification.setReferenceId(p_refId);
            notification.setType(p_refType);
            notification.setStatus(NotificationStatus.PREPARED);

            notification = notificationMessageRepository.save(notification);

            /* Specify which tokens will receive the notification. Currently we only send to one user.

               You can build a wide range of QueryHolders to target any set of users (keep in mind to
               change the Notification object also to not require User if it will be general notification)
             */
            final JpaNotificationQueryHolder queryHolder = new UserQueryHolder(p_userId);

            final List<NotificationResult> results = notify(notification, p_message, p_customData, queryHolder);
            saveResults(notification, results);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void saveResults(final NotificationMessage p_notification, final List<NotificationResult> p_results) {
        Long gcmCount = 0L;
        Long apnsCount = 0L;
        for (NotificationResult notificationResult : p_results) {
            if (notificationResult.getType() == NotificationPlatformType.APNS) {
                apnsCount += notificationResult.getTotalCount();
            } else {
                gcmCount += notificationResult.getTotalCount();
            }
        }
        p_notification.setApnsCount(apnsCount.intValue());
        p_notification.setGcmCount(gcmCount.intValue());
        p_notification.setDoneSending(DateTime.now());
        if (gcmCount == 0 && apnsCount == 0) {
            p_notification.setStatus(NotificationStatus.COMPLETED);
        } else {
            p_notification.setStatus(NotificationStatus.SENT);
        }

        notificationMessageRepository.save(p_notification);
    }

    private List<NotificationResult> notify(final NotificationMessage p_notification,
                                            final String p_notificationText,
                                            final Map<String, Object> p_customData,
                                            final JpaNotificationQueryHolder p_queryHolder) {
        final List<NotificationResult> results = new LinkedList<>();
        if (notifyProviders != null && notifyProviders.isEmpty()) {
            LOG.info("No notify providers. Notification won't be sent.");
        } else {
            LOG.debug("Sending notification for type {} with reference id {}", p_notification.getType(), p_notification.getReferenceId());

            final List<Long> notifiedUserIds = new LinkedList<>();
            for (final NotificationProvider notifyProvider : notifyProviders) {
                LOG.debug("\t --> invoking provider {} for id ", notifyProvider.getClass().getName(), p_notification.getReferenceId());
                final NotificationResult result = notifyProvider.pushNotificationMessage(p_notification, p_notificationText, p_customData, p_queryHolder);
                results.add(result);
            }

            LOG.debug("Sending notification for type {} with id {} finished. Notified {} unique user.",
                p_notification.getType(),
                p_notification.getReferenceId(),
                notifiedUserIds.size());
        }

        return results;
    }

    private List<NotificationResult> notify(String p_notificationText, Map<String, Object> p_customData, JpaNotificationQueryHolder p_queryHolder) {
        final List<NotificationResult> results = new LinkedList<>();
        if (notifyProviders != null && notifyProviders.isEmpty()) {
            LOG.info("No notify providers. Notification won't be sent.");
        } else {
            LOG.debug("Sending notification which will not be persisted");

            final List<Long> notifiedUserIds = new LinkedList<>();
            for (final NotificationProvider notifyProvider : notifyProviders) {
                LOG.debug("\t --> invoking provider {}", notifyProvider.getClass().getName());
                final NotificationResult result = notifyProvider.pushPlainTextMessage(p_notificationText, p_customData, p_queryHolder);
                results.add(result);
            }

            LOG.debug("Sending notification finished. Notified {} unique user.", notifiedUserIds.size());
        }

        return results;
    }

}
