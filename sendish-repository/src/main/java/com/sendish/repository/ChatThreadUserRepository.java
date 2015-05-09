package com.sendish.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.ChatThreadUser;
import com.sendish.repository.model.jpa.ChatThreadUserId;

@Transactional(propagation = Propagation.MANDATORY)
public interface ChatThreadUserRepository extends JpaRepository<ChatThreadUser, ChatThreadUserId> {
	
	List<ChatThreadUser> findByChatThreadId(Long chatThreadId);

}
