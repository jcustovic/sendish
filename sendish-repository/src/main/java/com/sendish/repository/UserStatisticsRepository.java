package com.sendish.repository;

import com.sendish.repository.model.jpa.UserStatistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

}
