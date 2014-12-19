package com.sendish.repository;

import com.sendish.repository.model.jpa.Photo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.uuid = ?2 AND p.deleted = false")
    Photo findByUserIdAndUuid(Long userId, String photoUUID);

    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.deleted = false")
    List<Photo> findByUserId(Long userId, Pageable pageRequest);

    @Query("SELECT p FROM Photo p WHERE p.id = ?1 AND p.user.id = ?2 AND p.deleted = false")
    Photo findByIdAndUserId(Long photoId, Long userId);

}
