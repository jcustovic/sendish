package com.sendish.repository;

import com.sendish.repository.model.jpa.ResizedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ResizedImageRepository extends JpaRepository<ResizedImage, Long> {

    @Query("SELECT ri FROM ResizedImage ri WHERE ri.image.id = ?1 AND ri.key = ?2")
    ResizedImage findByUuidAndKey(Long imageId, String key);

}
