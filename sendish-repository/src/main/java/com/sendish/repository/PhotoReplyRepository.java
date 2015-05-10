package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoReply;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface PhotoReplyRepository extends JpaRepository<PhotoReply, Long> {
	
	PhotoReply findByUserIdAndPhotoId(Long userId, Long photoId);
	
	Page<PhotoReply> findByPhotoId(Long photoId, Pageable page);

	PhotoReply findByUuid(String photoReplyUUID);

}
