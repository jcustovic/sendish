package com.sendish.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.PhotoSendStatus;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.PhotoStatus;

@Transactional(readOnly = true)
public interface PhotoSendingDetailsRepository extends JpaRepository<PhotoSendingDetails, Long> {

	Page<PhotoSendingDetails> findByPhotoStatusAndSendStatus(PhotoStatus photoStatus, PhotoSendStatus sendStatus, Pageable page);

}
