package com.sendish.repository;

import com.sendish.repository.model.jpa.AutoSendingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface AutoSendingPhotoRepository extends JpaRepository<AutoSendingPhoto, Long> {

    @Query(value = "SELECT asp FROM AutoSendingPhoto asp WHERE asp.active = true AND asp.city IS NULL AND asp.country IS NULL")
    List<AutoSendingPhoto> findAllActiveDefault();

}
