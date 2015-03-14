package com.sendish.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.PhotoComment;

@Transactional(propagation = Propagation.MANDATORY)
public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {

	@Query("SELECT pc FROM PhotoComment pc where pc.photo.id = ?1 AND pc.deleted = false")
	List<PhotoComment> findByPhotoId(Long photoId, Pageable page);

}
