package com.sendish.api.web.controller.api.v1;

import com.sendish.api.service.ResizeImageService;
import com.sendish.api.service.impl.ImageServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.repository.model.jpa.Image;
import com.sendish.repository.model.jpa.ResizedImage;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping("/api/v1.0/images")
@Api(value = "images", description = "Get images (not photos!)")
public class ImagesController {

    @Autowired
    private FileStore fileStore;

    @Autowired
    private ImageServiceImpl imageService;

    @Autowired
    @Qualifier("retryableResizeImageService")
    private ResizeImageService resizedImageService;

    @RequestMapping(value = "/{imageUUID}", method = RequestMethod.GET)
    @ApiOperation(value = "View image in original size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewOriginal(@PathVariable String imageUUID, WebRequest webRequest) {
        Image image = imageService.findByUuid(imageUUID);

        if (image == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return viewImage(webRequest, image.getCreatedDate(), image.getContentType(), image.getSize(), image.getStorageId());
        }
    }

    @RequestMapping(value = "/{imageUUID}/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View image in different size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewReceived(@PathVariable String imageUUID, @PathVariable String sizeKey, WebRequest webRequest) {
        Image image = imageService.findByUuid(imageUUID);

        if (image == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
        	try {
	            ResizedImage resizedImage = resizedImageService.getResizedImage(image.getId(), sizeKey);
	
	            return viewImage(webRequest, resizedImage.getCreatedDate(), image.getContentType(), resizedImage.getSize(), resizedImage.getStorageId());
        	} catch (Exception e) {
                return viewImage(webRequest, image.getCreatedDate(), image.getContentType(), image.getSize(), image.getStorageId());
            }
        }
    }

    private ResponseEntity<InputStreamResource> viewImage(WebRequest webRequest, DateTime createdDate, String contentType, Long size, String storageId) {
        if (webRequest.checkNotModified(createdDate.getMillis())) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        try {
            InputStreamResource isr = new InputStreamResource(fileStore.getAsInputStream(storageId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(contentType));
            headers.setContentLength(size);

            return new ResponseEntity<>(isr, headers, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
