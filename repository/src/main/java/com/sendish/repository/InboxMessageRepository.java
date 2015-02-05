package com.sendish.repository;

import com.sendish.repository.model.jpa.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface InboxMessageRepository extends JpaRepository<InboxMessage, Long> {
}
