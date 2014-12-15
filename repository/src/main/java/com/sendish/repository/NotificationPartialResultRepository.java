package com.sendish.repository;

import com.sendish.repository.model.jpa.NotificationPartialResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationPartialResultRepository extends JpaRepository<NotificationPartialResult, Long> {

    List<NotificationPartialResult> findByNotificationId(Long p_id);

}
