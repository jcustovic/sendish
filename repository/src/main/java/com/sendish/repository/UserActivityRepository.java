package com.sendish.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.UserActivity;

@Transactional(readOnly = true)
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

}
