package com.sendish.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.PhotoVoteRepository;
import com.sendish.repository.model.jpa.PhotoVote;
import com.sendish.repository.model.jpa.PhotoVoteId;

@Service
@Transactional
public class PhotoVoteServiceImpl {
	
	@Autowired
	private PhotoVoteRepository photoVoteRepository;
	
	public PhotoVote findOne(Long photoId, Long userId) {
		return photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
	}

}
