package com.sendish.push.notification;

import java.util.Date;
import java.util.Map;

public interface ApnsNotificationProvider extends NotificationProvider {

    Map<String, Date> getInactiveDevicesDev();

    Map<String, Date> getInactiveDevices();

}
