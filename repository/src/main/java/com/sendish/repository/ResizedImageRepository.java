package com.sendish.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.ResizedImage;

@Transactional(propagation = Propagation.MANDATORY)
public interface ResizedImageRepository extends JpaRepository<ResizedImage, Long> {

    ResizedImage findByImageIdAndKey(Long imageId, String key);

}
