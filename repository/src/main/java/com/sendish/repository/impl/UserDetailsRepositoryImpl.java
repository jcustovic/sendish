package com.sendish.repository.impl;

import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.UserDetailsRepositoryCustom;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserDetails;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.LinkedList;
import java.util.List;

public class UserDetailsRepositoryImpl implements UserDetailsRepositoryCustom {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Override
    public Page<UserDetails> searchUsersForSendingPool(DateTime latestUserPhotoReceivedDate, int size) {
        final Specification<UserDetails> spec = (p_root, p_criteriaQuery, p_criteriaBuilder) -> {
        	final List<Predicate> andPredicates = new LinkedList<>();
        	DateTime now = DateTime.now();
        	
        	// (lastInteractionTime IS NOT NULL AND lastInteractionTime > today - 10days)
        	Path<DateTime> lastInteractionTime = p_root.get("lastInteractionTime");
        	andPredicates.add(p_criteriaBuilder.and(
        			p_criteriaBuilder.isNotNull(lastInteractionTime), 
        			p_criteriaBuilder.greaterThan(lastInteractionTime, now.minusDays(10))
        	));
        	
        	// (receiveAllowedTime IS NULL OR receiveAllowedTime >= now)
        	Path<DateTime> receiveAllowedTime = p_root.get("receiveAllowedTime");
        	andPredicates.add(p_criteriaBuilder.or(
        			p_criteriaBuilder.isNull(receiveAllowedTime), 
        			p_criteriaBuilder.greaterThanOrEqualTo(receiveAllowedTime, now)
        	));
        	
        	if (latestUserPhotoReceivedDate != null)  {
	        	// (lastReceivedTime IS NULL OR lastReceivedTime >= lastUserPhotoReceivedDate)
	        	Path<DateTime> lastReceivedTime = p_root.get("lastReceivedTime");
	        	andPredicates.add(p_criteriaBuilder.or(
	        			p_criteriaBuilder.isNull(lastReceivedTime), 
	        			p_criteriaBuilder.greaterThanOrEqualTo(lastReceivedTime, latestUserPhotoReceivedDate)
	        	));
        	}
        	
        	// disabled == false AND deleted = false
        	final Path<User> user = p_root.get("user");
        	andPredicates.add(p_criteriaBuilder.isFalse(user.get("deleted")));
        	andPredicates.add(p_criteriaBuilder.isFalse(user.get("disabled")));
        	
            final Predicate andPredicate = p_criteriaBuilder.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
            
            return andPredicate;
        };

        // ORDER BY lastReceivedTime ASC
        return userDetailsRepository.findAll(spec, new PageRequest(0, size, Direction.ASC, "lastReceivedTime"));
    }

}
