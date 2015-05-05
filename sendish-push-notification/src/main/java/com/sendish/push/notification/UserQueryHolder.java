package com.sendish.push.notification;

import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.GcmPushToken;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class UserQueryHolder implements JpaNotificationQueryHolder {

    private static final Logger  LOG = LoggerFactory.getLogger(UserQueryHolder.class);

    private final transient Long userId;

    public UserQueryHolder(final Long p_userId) {
        super();
        userId = p_userId;
    }

    @Override
    public final Specification<GcmPushToken> getGcmQuery() {
        LOG.debug("Building GCM query --> {}", toString());

        return this::buildQuery;
    }

    @Override
    public final Specification<ApnsPushToken> getApnsQuery() {
        LOG.debug("Building APNS query --> {}", toString());

        return this::buildQuery;
    }

    private Predicate buildQuery(final Root<?> p_root, final CriteriaQuery<?> p_query, final CriteriaBuilder p_builder) {
        return p_builder.and(p_builder.equal(p_root.get("user").get("id").as(Long.class), userId));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("userId", userId).toString();
    }

}
