package com.sendish.api.web.controller.validator;

import com.sendish.api.dto.ReportPhotoReplyDto;
import com.sendish.api.service.impl.PhotoReplyServiceImpl;
import com.sendish.repository.model.jpa.PhotoReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ReportPhotoReplyValidator implements Validator {

	@Autowired
	private PhotoReplyServiceImpl photoReplyService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ReportPhotoReplyDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            // IF there are errors we won't continue
            return;
        }
        ReportPhotoReplyDto reportPhotoReply = (ReportPhotoReplyDto) target;
        
        PhotoReply photoReply = photoReplyService.findOne(reportPhotoReply.getPhotoReplyId());
        
        if (photoReply == null) {
        	errors.rejectValue("photoReplyId", null, "Photo reply not found");
        } else if (!(photoReply.getUser().getId().equals(reportPhotoReply.getUserId())
        		|| photoReply.getPhoto().getUser().getId().equals(reportPhotoReply.getUserId()))) {
        	errors.rejectValue("photoReplyId", null, "Photo reply not found");
        } else if (StringUtils.hasText(photoReply.getReportText())) {
            errors.rejectValue("photoReplyId", null, "Photo reply already reported");
        }
    }

}
