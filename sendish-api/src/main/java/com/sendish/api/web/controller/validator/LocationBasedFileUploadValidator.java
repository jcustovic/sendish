package com.sendish.api.web.controller.validator;

import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.util.ImageUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Component
public class LocationBasedFileUploadValidator implements Validator {

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
        return LocationBasedFileUpload.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            // IF there are errors we won't continue
            return;
        }
        LocationBasedFileUpload fileUpload = (LocationBasedFileUpload) target;
        
        if (fileUpload.getImage() == null) {
        	errors.rejectValue("image", null, "Image/File not provided.");
        	return;
        }

        if (fileUpload.getLatitude() == null
                || fileUpload.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0
                || fileUpload.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
            errors.rejectValue("latitude", null, "Latitude value must be between -90 and 90");
        }

        if (fileUpload.getLongitude() == null
                || fileUpload.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0
                || fileUpload.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
            errors.rejectValue("longitude", null, "Longitude value must be between -180 and 180");
        }

        if (!allowedContentTypes.contains(fileUpload.getImage().getContentType())) {
            errors.rejectValue("image", null, "Content type not allowed");
        }

        Dimension dimension;
        try {
            dimension = ImageUtils.getDimension(fileUpload.getImage().getInputStream());
            if (dimension.getHeight() != IMAGE_HEIGHT || dimension.getWidth() != IMAGE_WIDTH) {
                errors.rejectValue("image", null, "Image size must be exactly 640px in width X 640px in height");
            }
        } catch (IOException e) {
            errors.rejectValue("image", null, "Cannot read image dimensions for file " + fileUpload.getImage().getName());
        }
    }

}
