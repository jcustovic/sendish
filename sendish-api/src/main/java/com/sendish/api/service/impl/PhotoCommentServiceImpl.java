package com.sendish.api.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoComment;
import com.sendish.repository.model.jpa.User;

@Service
public class PhotoCommentServiceImpl {
	
	private static final int COMMENT_PAGE_SIZE = 20;
	
	@Autowired
	private PhotoCommentRepository photoCommentRepository;
	
	@Autowired
	private PhotoRepository photoRepository;
	
	@Autowired
	private UserRepository userRepository;

	public PhotoComment save(Long photoId, String comment, Long userId) {
		Photo photo = photoRepository.findOne(photoId);
		User user = userRepository.findOne(userId);
		
		PhotoComment photoComment = new PhotoComment();
		photoComment.setPhoto(photo);
		photoComment.setUser(user);
		photoComment.setComment(comment);
		// TODO: Maybe restrict to only my photos and received photos?
		// TODO: Increase comment count on photo
		
		return photoCommentRepository.save(photoComment);
	}
	
	public List<PhotoComment> findByPhotoId(Long photoId, Integer page) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photoId, 
        		new PageRequest(page, COMMENT_PAGE_SIZE, Direction.DESC, "createdDate"));

        return photoComments;
    }

	public void like(Long photoId, Long userId) {
		// TODO Auto-generated method stub
	}

	public void dislike(Long photoId, Long userId) {
		// TODO Auto-generated method stub
	}

}
