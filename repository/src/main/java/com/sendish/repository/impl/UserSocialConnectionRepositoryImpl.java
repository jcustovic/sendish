package com.sendish.repository.impl;

import com.sendish.repository.UserSocialConnectionRepository;
import com.sendish.repository.UserSocialConnectionRepositoryCustom;
import com.sendish.repository.model.jpa.UserSocialConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;

import javax.persistence.criteria.Predicate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserSocialConnectionRepositoryImpl implements UserSocialConnectionRepositoryCustom {

    @Autowired
    private transient UserSocialConnectionRepository userSocialConnectionRepository;

    @Override
    public final List<UserSocialConnection> findConnectionsToUsers(final Long p_userId, final MultiValueMap<String, String> p_providerUsers) {
        final Specification<UserSocialConnection> spec = (p_root, p_criteriaQuery, p_criteriaBuilder) -> {
            p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("userId").as(Long.class), p_userId));

            final List<Predicate> orPredicates = new LinkedList<>();
            for (final Map.Entry<String, List<String>> entry : p_providerUsers.entrySet()) {
                final String providerId = entry.getKey();

                orPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("providerId"), providerId),
                        p_root.get("providerUserId").in(entry.getValue())));
            }

            final Predicate orPredicate = p_criteriaBuilder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
            return p_criteriaBuilder.and(orPredicate);
        };

        return userSocialConnectionRepository.findAll(spec);
    }

    @Override
    public UserSocialConnection findByOAuth2(String providerId, String accessToken, String refreshToken) {
        final Specification<UserSocialConnection> spec = (p_root, p_criteriaQuery, p_criteriaBuilder) -> {
            final List<Predicate> andPredicates = new LinkedList<>();

            andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("providerId").as(String.class), providerId)));
            andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("accessToken").as(String.class), accessToken)));

            if (refreshToken == null) {
                andPredicates.add(p_criteriaBuilder.and(p_root.get("refreshToken").isNull()));
            } else {
                andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("refreshToken").as(String.class), refreshToken)));
            }

            final Predicate andPredicate = p_criteriaBuilder.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
            return p_criteriaBuilder.and(andPredicate);
        };

        return userSocialConnectionRepository.findOne(spec);
    }

    @Override
    public UserSocialConnection findByOAuth1(String providerId, String tokenValue, String tokenSecret) {
        final Specification<UserSocialConnection> spec = (p_root, p_criteriaQuery, p_criteriaBuilder) -> {
            final List<Predicate> andPredicates = new LinkedList<>();

            andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("providerId").as(String.class), providerId)));
            andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("accessToken").as(String.class), tokenValue)));
            andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("secret").as(String.class), tokenSecret)));

            final Predicate andPredicate = p_criteriaBuilder.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
            return p_criteriaBuilder.and(andPredicate);
        };

        return userSocialConnectionRepository.findOne(spec);
    }

}
