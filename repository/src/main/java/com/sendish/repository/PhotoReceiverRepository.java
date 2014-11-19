package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoReceiverRepository extends JpaRepository<PhotoReceiver, Long> {

}
