package com.sendish.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.PhotoVote;
import com.sendish.repository.model.jpa.PhotoVoteId;

@Transactional(propagation = Propagation.MANDATORY)
public interface PhotoVoteRepository extends JpaRepository<PhotoVote, PhotoVoteId> {

}
