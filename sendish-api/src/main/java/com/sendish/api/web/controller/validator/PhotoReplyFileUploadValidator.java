package com.sendish.api.web.controller.validator;

import java.awt.Dimension;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.sendish.api.dto.PhotoReplyFileUpload;
import com.sendish.api.service.impl.PhotoReplyServiceImpl;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.model.jpa.PhotoReceiver;
import com.sendish.repository.model.jpa.PhotoReply;

@Component
public class PhotoReplyFileUploadValidator implements Validator {
	
	public static final int IMAGE_HEIGHT = 640;
    public static final int IMAGE_WIDTH = 640;

    private static List<String> allowedContentTypes;
    static {
        allowedContentTypes = new LinkedList<>();

        allowedContentTypes.add(MediaType.IMAGE_PNG_VALUE);
        allowedContentTypes.add(MediaType.IMAGE_JPEG_VALUE);
    }
	
	@Autowired
	private PhotoServiceImpl photoService;
	
	@Autowired
	private PhotoReplyServiceImpl photoReplyService;

    @Override
    public boolean supports(Class<?> clazz) {
        return PhotoReplyFileUpload.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            // IF there are errors we won't continue
            return;
        }
        PhotoReplyFileUpload photoReplyUpload = (PhotoReplyFileUpload) target;
        
        if (photoReplyUpload.getImage() == null) {
        	errors.rejectValue("image", null, "Image/File not provided.");
        	return;
        }

        if (!allowedContentTypes.contains(photoReplyUpload.getImage().getContentType())) {
            errors.rejectValue("image", null, "Content type not allowed");
        }
        
        PhotoReceiver photoReceiver = photoService.findReceivedByPhotoIdAndUserId(photoReplyUpload.getPhotoId(), photoReplyUpload.getUserId());
        if (photoReceiver == null) {
        	errors.rejectValue("photoId", null, "Photo not found");
        } else {
        	PhotoReply photoReply = photoReplyService.findByUserIdAndPhotoId(photoReplyUpload.getUserId(), photoReplyUpload.getPhotoId());
        	if (photoReply != null) {
        		errors.rejectValue("photoId", null, "Photo reply already sent for specified photo");	
        	}
        }

        if (!errors.hasErrors()) {
            Dimension dimension;
            try {
                dimension = ImageUtils.getDimension(photoReplyUpload.getImage().getInputStream());
                if (dimension.getHeight() != IMAGE_HEIGHT || dimension.getWidth() != IMAGE_WIDTH) {
                    errors.rejectValue("image", null, "Image size must be exactly 640px in width X 640px in height");
                }
            } catch (IOException e) {
                errors.rejectValue("image", null, "Cannot read image dimensions for file " + photoReplyUpload.getImage().getName());
            }
        }
    }

}
