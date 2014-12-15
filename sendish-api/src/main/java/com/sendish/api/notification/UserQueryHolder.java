package com.sendish.api.notification;

import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.GcmPushToken;
import com.sendish.repository.model.jpa.User;
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

    private final transient User user;

    public UserQueryHolder(final User p_user) {
        super();
        user = p_user;
    }

    @Override
    public final Specification<GcmPushToken> getGcmQuery() {
        LOG.debug("Building GCM query --> {}", toString());

        return (p_root, p_query, p_builder) -> buildQuery(p_root, p_query, p_builder);
    }

    @Override
    public final Specification<ApnsPushToken> getApnsQuery() {
        LOG.debug("Building APNS query --> {}", toString());

        return (p_root, p_query, p_builder) -> buildQuery(p_root, p_query, p_builder);
    }

    private Predicate buildQuery(final Root<?> p_root, final CriteriaQuery<?> p_query, final CriteriaBuilder p_builder) {
        return p_builder.and(p_builder.equal(p_root.get("user").as(User.class), user));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("user", user).toString();
    }

}
