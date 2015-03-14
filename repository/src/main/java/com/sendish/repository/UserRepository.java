package com.sendish.repository;

import com.sendish.repository.model.jpa.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;

@Transactional(propagation = Propagation.MANDATORY)
public interface UserRepository extends JpaRepository<User, Long> {

    @QueryHints({
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = "com.sendish.repository.UserRepository.findByUsername")
    })
    User findByUsernameIgnoreCaseOrEmailIgnoreCase(String p_username, String p_email);

    @QueryHints({
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = "com.sendish.repository.UserRepository.findByUsername")
    })
    User findByUsernameIgnoreCase(String username);

}
