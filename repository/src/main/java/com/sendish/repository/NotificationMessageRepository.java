package com.sendish.repository;

import com.sendish.repository.model.jpa.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {
}
