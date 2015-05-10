package com.sendish.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.ChatMessage;

@Transactional(propagation = Propagation.MANDATORY)
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByChatThreadId(Long chatThreadId, Pageable page);

}
