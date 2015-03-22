package com.sendish.api.web.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.sendish.api.dto.NewCommentDto;
import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoComment;

@Component
public class NewCommentValidator implements Validator {

    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private PhotoCommentRepository photoCommentRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NewCommentDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
    	NewCommentDto newComment = (NewCommentDto) target;
    	
    	Photo photo = photoRepository.findOne(newComment.getPhotoId());
    	if (photo == null) {
    		errors.rejectValue("photoId", null, "Photo not found");
    		return;
    	}
    	
    	if (newComment.getReplyToId() != null) {
    		PhotoComment replyToComment = photoCommentRepository.findOne(newComment.getReplyToId());
    		if (replyToComment == null) {
    			errors.rejectValue("replyToId", null, "Reply to comment not found");
    		} else if (replyToComment.getUser().getId().equals(newComment.getUserId())) {
    			errors.rejectValue("replyToId", null, "You cannot reply to your own comment");
    		}
    	}
    }

}
