package com.sendish.api.notification;

import com.sendish.repository.model.jpa.User;

import java.util.Map;

public interface AsyncNotificationProvider {

    void notifyAsync(Long p_refId, String p_refType, String p_message, Map<String, Object> p_data, User p_user);

}
