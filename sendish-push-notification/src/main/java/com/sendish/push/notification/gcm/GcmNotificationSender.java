package com.sendish.push.notification.gcm;

import com.google.android.gcm.server.*;
import com.sendish.repository.GcmPushTokenRepository;
import com.sendish.repository.NotificationMessageRepository;
import com.sendish.repository.NotificationPartialResultRepository;
import com.sendish.repository.model.jpa.GcmPushToken;
import com.sendish.repository.model.jpa.NotificationMessage;
import com.sendish.repository.model.jpa.NotificationPartialResult;
import com.sendish.repository.model.jpa.NotificationPlatformType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GcmNotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(GcmNotificationSender.class);

    private final Sender sender;

    @Autowired
    private GcmPushTokenRepository gcmPushTokenRepository;

    @Autowired
    private NotificationPartialResultRepository notificationPartialResultRepository;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    public GcmNotificationSender(Sender sender) {
        this.sender = sender;
    }

    @Transactional
    public void sendNotificationMessage(List<String> refIds, Message message, Long notificationMsgId) throws IOException {
        ArrayList<String> devices = new ArrayList<>(refIds);

        NotificationMessage notification = notificationMessageRepository.findOne(notificationMsgId);
        NotificationPartialResult notificationResult = new NotificationPartialResult();
        notificationResult.setNotification(notification);
        notificationResult.setPlatformType(NotificationPlatformType.GCM);
        notificationResult.setSendDate(DateTime.now());
        notificationResult.setTotalCount(devices.size());

        MulticastResult resultResponse = sendPushMessage(refIds, message);

        notificationResult.setResponseDate(DateTime.now());
        notificationResult.setFailureCount(resultResponse.getFailure());
        notificationPartialResultRepository.save(notificationResult);
    }

    public MulticastResult sendPushMessage(List<String> refIds, Message message) throws IOException {
        ArrayList<String> devices = new ArrayList<>(refIds);
        MulticastResult resultResponse = sender.send(message, devices, 3);

        LOG.debug("Notification sent --> total: {}, success: {}, failure: {}",
                resultResponse.getTotal(), resultResponse.getSuccess(), resultResponse.getFailure());

        dealWithErrors(devices, resultResponse);

        return resultResponse;
    }

    private void dealWithErrors(final ArrayList<String> devices, final MulticastResult p_resultResponse) {
        int i = 0;
        for (final Result result : p_resultResponse.getResults()) {
            final String regId = devices.get(i++);
            if (result.getMessageId() == null) {
                final String error = result.getErrorCodeName();
                if (Constants.ERROR_INVALID_REGISTRATION.equals(error) || Constants.ERROR_NOT_REGISTERED.equals(error)) {
                    LOG.trace("GCM token {} is inactive. Deleting...", regId);
                    gcmPushTokenRepository.deleteByToken(regId);
                } else {
                    // TODO: Maybe try resolve other types of errors (brake the loop in case of some)?
                    LOG.error("GCM error {} on regId {}", error, regId);
                }
            } else {
                final String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    LOG.trace("GCM token {} changed to {}. Updating...", regId, canonicalRegId);
                    final GcmPushToken canonicalToken = gcmPushTokenRepository.findByToken(canonicalRegId);
                    if (canonicalToken == null) {
                        // We don't have the token yet so update
                        gcmPushTokenRepository.updateToken(regId, canonicalRegId);
                    } else {
                        // Somehow we have it. Only delete the old one.
                        gcmPushTokenRepository.deleteByToken(regId);
                    }
                }
            }
        }
    }

}
