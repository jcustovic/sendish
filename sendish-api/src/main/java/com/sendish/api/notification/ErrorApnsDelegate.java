package com.sendish.api.notification;

import com.notnoop.apns.ApnsDelegateAdapter;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.exceptions.ApnsDeliveryErrorException;
import com.sendish.repository.ApnsPushTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorApnsDelegate extends ApnsDelegateAdapter {

    private static final Logger               LOG = LoggerFactory.getLogger(ErrorApnsDelegate.class);

    @Autowired
    private transient ApnsPushTokenRepository apnsPushTokenRepository;

    @Override
    public final void messageSendFailed(final ApnsNotification p_message, final Throwable p_exception) {
        if (p_message == null) {
            LOG.error("Notification has been rejected by Apple", p_exception);
        } else {
            final String deviceToken = Utilities.encodeHex(p_message.getDeviceToken());
            LOG.warn("Send failed --> identifier {}, token {}, error: {}", p_message.getIdentifier(), deviceToken, p_exception.getMessage());
            if (p_exception instanceof ApnsDeliveryErrorException) {
                final ApnsDeliveryErrorException apnsError = (ApnsDeliveryErrorException) p_exception;
                if (DeliveryError.INVALID_TOKEN.equals(apnsError.getDeliveryError())) {
                    LOG.info("Sending failed because of invalid token. Removing it from database...");
                    // Lowercase used because apple returns uppercase!
                    apnsPushTokenRepository.deleteByToken(deviceToken.toLowerCase());
                }
            }
        }
    }

    @Override
    public final void connectionClosed(final DeliveryError p_error, final int p_messageIdentifier) {
        if (!DeliveryError.NO_ERROR.equals(p_error)) {
            LOG.warn("Connection closed. Received error code {}, identifier {}", p_error, p_messageIdentifier);
        }
    }

}
