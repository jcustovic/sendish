package com.sendish.api.web.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.sendish.api.dto.NewPhotoReplyMessageDto;
import com.sendish.api.service.impl.PhotoReplyServiceImpl;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.PhotoReply;

@Component
public class NewPhotoReplyMessageValidator implements Validator {
	
	@Autowired
	private PhotoServiceImpl photoService;
	
	@Autowired
	private PhotoReplyServiceImpl photoReplyService;

    @Override
    public boolean supports(Class<?> clazz) {
        return NewPhotoReplyMessageDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            // IF there are errors we won't continue
            return;
        }
        NewPhotoReplyMessageDto newPhotoReplyMsg = (NewPhotoReplyMessageDto) target;
        
        PhotoReply photoReply = photoReplyService.findOne(newPhotoReplyMsg.getPhotoReplyId());
        
        if (photoReply == null) {
        	errors.rejectValue("photoReplyId", null, "Photo reply not found");
        } else if (!(photoReply.getUser().getId().equals(newPhotoReplyMsg.getUserId()) 
        		|| photoReply.getPhoto().getUser().getId().equals(newPhotoReplyMsg.getUserId()))) {
        	errors.rejectValue("photoReplyId", null, "Photo reply not found");
        }
    }

}
