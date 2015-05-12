package com.sendish.repository;

import java.util.List;

import com.sendish.repository.model.jpa.ChatThread;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {
	
	@Query("SELECT ctu.chatThread FROM ChatThreadUser ctu JOIN ctu.chatThread ct WHERE ctu.user.id = ?1 "
			+ "AND ct.photoReply.deleted = FALSE "
			+ "AND (ct.photoReply.user.id = ?1 OR ct.photoReply.photo.user.id = ?1)")
	List<ChatThread> findPhotoReplyThreadsByUserId(Long userId, Pageable page);
	
	ChatThread findByPhotoReplyId(Long photoReplyId);

	List<ChatThread> findByPhotoReplyPhotoId(Long photoId, Pageable page);

}
