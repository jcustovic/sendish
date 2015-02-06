package com.sendish.repository.impl;

import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.UserDetailsRepositoryCustom;
import com.sendish.repository.model.jpa.UserDetails;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.LinkedList;
import java.util.List;

public class UserDetailsRepositoryImpl implements UserDetailsRepositoryCustom {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Override
    public Page<UserDetails> searchUsersForSendingPool(DateTime lastCheckDate, int size) {
        final Specification<UserDetails> spec = (p_root, p_criteriaQuery, p_criteriaBuilder) -> {
            // TODO: Implement
            // UserDetails: (lastInteractionTime not null AND lastInteractionTime > today - 10days) AND (receiveAllowedTime IS NULL OR receiveAllowedTime >= now)
            // AND (lastReceivedTime >= redis (max lastReceivedTime) or lastReceivedTime == null) order by lastReceivedTime ASC
            // User: disabled = false, deleted = false

            final List<Predicate> andPredicates = new LinkedList<>();
            /*
            p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("userId").as(Long.class), p_userId));

            for (final Map.Entry<String, List<String>> entry : p_providerUsers.entrySet()) {
                final String providerId = entry.getKey();

                andPredicates.add(p_criteriaBuilder.and(p_criteriaBuilder.equal(p_root.get("providerId"), providerId),
                        p_root.get("providerUserId").in(entry.getValue())));
            }
             */

            final Predicate andPredicate = p_criteriaBuilder.or(andPredicates.toArray(new Predicate[andPredicates.size()]));
            return p_criteriaBuilder.and(andPredicate);
        };

        return userDetailsRepository.findAll(spec, new PageRequest(0, size));
    }

}
