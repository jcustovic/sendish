package com.sendish.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.PhotoComment;

@Transactional(readOnly = true)
public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {

	List<PhotoComment> findByPhotoId(Long photoId, Pageable page);

}
