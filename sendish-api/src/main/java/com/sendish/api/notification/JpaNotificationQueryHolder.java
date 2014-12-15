package com.sendish.api.notification;

import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.GcmPushToken;
import org.springframework.data.jpa.domain.Specification;

public interface JpaNotificationQueryHolder {

    Specification<GcmPushToken> getGcmQuery();

    Specification<ApnsPushToken> getApnsQuery();

}
