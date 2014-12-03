package com.sendish.api.web.controller.validator;

import com.sendish.api.dto.LocationBasedFileUpload;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Component
public class LocationBasedFileUploadValidator implements Validator {

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
        LocationBasedFileUpload fileUpload = (LocationBasedFileUpload) target;

        if (fileUpload.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 || fileUpload.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
            errors.rejectValue("latitude", null, "Latitude value must be between -90 and 90");
        }

        if (fileUpload.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 || fileUpload.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
            errors.rejectValue("longitude", null, "Longitude value must be between -180 and 180");
        }

        if (!allowedContentTypes.contains(fileUpload.getImage().getContentType())) {
            errors.rejectValue("image", null, "Content type not allowed");
        }
    }

}
