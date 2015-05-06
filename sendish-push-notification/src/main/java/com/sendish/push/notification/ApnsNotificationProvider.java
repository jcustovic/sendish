package com.sendish.push.notification;

import com.sendish.repository.model.jpa.ApnsPushToken;

import java.util.Date;
import java.util.Map;

public interface ApnsNotificationProvider extends NotificationProvider {

    Map<String, Date> getInactiveDevicesDev();

    Map<String, Date> getInactiveDevices();

    void sendMessage(ApnsPushToken tokens, String message);

}
