package com.sendish.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.ResizedPhoto;

@Transactional(propagation = Propagation.MANDATORY)
public interface ResizedPhotoRepository extends JpaRepository<ResizedPhoto, Long> {

    ResizedPhoto findByPhotoIdAndKey(Long photoId, String key);

}
