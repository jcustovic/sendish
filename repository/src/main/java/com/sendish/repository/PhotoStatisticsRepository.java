package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoStatisticsRepository extends JpaRepository<PhotoStatistics, Long> {

}
