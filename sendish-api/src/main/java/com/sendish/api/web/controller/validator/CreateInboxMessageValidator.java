package com.sendish.api.web.controller.validator;

import com.sendish.api.dto.admin.CreateInboxMessage;
import com.sendish.api.util.ImageUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
public class CreateInboxMessageValidator implements Validator {

    public static final int IMAGE_HEIGHT = 640;
    public static final int IMAGE_WIDTH = 640;

    private static List<String> allowedContentTypes;
    static {
        allowedContentTypes = new LinkedList<>();

        allowedContentTypes.add(MediaType.IMAGE_PNG_VALUE);
        allowedContentTypes.add(MediaType.IMAGE_JPEG_VALUE);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateInboxMessage.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            // IF there are errors we won't continue
            return;
        }
        CreateInboxMessage createInboxMessage = (CreateInboxMessage) target;
        
        if (createInboxMessage.getImage() == null) {
        	errors.rejectValue("image", null, "Image/File not provided.");
        	return;
        }

        if (createInboxMessage.getImage().getName().length() > 128) {
            errors.rejectValue("image", null, "Filename must be < 128 chars");
        }

        if (!allowedContentTypes.contains(createInboxMessage.getImage().getContentType())) {
            errors.rejectValue("image", null, "Content type not allowed");
        }

        if (!errors.hasErrors()) {
            Dimension dimension;
            try {
                dimension = ImageUtils.getDimension(createInboxMessage.getImage().getInputStream());
                if (dimension.getHeight() != IMAGE_HEIGHT || dimension.getWidth() != IMAGE_WIDTH) {
                    errors.rejectValue("image", null, "Image size must be exactly 640px in width X 640px in height");
                }
            } catch (IOException e) {
                errors.rejectValue("image", null, "Cannot read image dimensions for file " + createInboxMessage.getImage().getName());
            }
        }
    }

}
