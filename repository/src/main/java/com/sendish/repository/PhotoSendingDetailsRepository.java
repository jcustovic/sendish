package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoSendingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoSendingDetailsRepository extends JpaRepository<PhotoSendingDetails, Long> {

}
