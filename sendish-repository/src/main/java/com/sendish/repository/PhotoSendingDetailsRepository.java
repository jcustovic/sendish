package com.sendish.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.PhotoSendStatus;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.PhotoStatus;

@Transactional(propagation = Propagation.MANDATORY)
public interface PhotoSendingDetailsRepository extends JpaRepository<PhotoSendingDetails, Long> {

	@Query("SELECT psd.id FROM PhotoSendingDetails psd WHERE psd.photoStatus = ?1 AND psd.sendStatus = ?2")
	Page<Long> findIdsByPhotoStatusAndSendStatus(PhotoStatus photoStatus, PhotoSendStatus sendStatus, Pageable page);
	
	@Query("SELECT psd.id FROM PhotoSendingDetails psd WHERE psd.photoStatus = 'TRAVELING' AND psd.sendStatus IN ('SENT', 'NO_USER')")
	Page<Long> findTravelingPhotoIdsByLastSentGreatherThan(Pageable page);

}
