package com.sendish.repository;

import com.sendish.repository.model.jpa.ChatThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {

}
