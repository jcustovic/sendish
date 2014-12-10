package com.sendish.repository;

import com.sendish.repository.model.jpa.PhotoCommentVote;
import com.sendish.repository.model.jpa.PhotoCommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoCommentVoteRepository extends JpaRepository<PhotoCommentVote, PhotoCommentVoteId> {
}
