package com.sendish.repository;

import com.sendish.repository.model.jpa.Photo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    Photo findByUuid(String uuid);

    Photo findByUserIdAndUuid(Long userId, String photoUUID);

    List<Photo> findByUserId(Long userId, Pageable pageRequest);

    Photo findByIdAndUserId(Long photoId, Long userId);

}
