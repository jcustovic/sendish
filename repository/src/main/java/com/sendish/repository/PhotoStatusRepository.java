package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoStatusRepository extends JpaRepository<PhotoStatus, Long> {

}
