package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {

}
