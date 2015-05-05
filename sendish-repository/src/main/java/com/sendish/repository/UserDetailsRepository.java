package com.sendish.repository;

import com.sendish.repository.model.jpa.UserDetails;

import com.sendish.repository.springframework.data.jpa.querydsl.CustomQueryDslJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface UserDetailsRepository extends CustomQueryDslJpaRepository<UserDetails, Long> {

}
