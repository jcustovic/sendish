package com.sendish.repository;

import com.sendish.repository.model.jpa.ResizedPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ResizedPhotoRepository extends JpaRepository<ResizedPhoto, Long> {

    @Query("SELECT rp FROM ResizedPhoto rp WHERE rp.photo.id = ?1 AND rp.photo.deleted = false AND rp.key = ?2")
    ResizedPhoto findByUuidAndKey(Long photoId, String key);

}
