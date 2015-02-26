package com.sendish.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.ResizedImage;

@Transactional(readOnly = true)
public interface ResizedImageRepository extends JpaRepository<ResizedImage, Long> {

    ResizedImage findByImageIdAndKey(Long imageId, String key);

}
