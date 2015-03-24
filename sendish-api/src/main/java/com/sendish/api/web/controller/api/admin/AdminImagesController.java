package com.sendish.api.web.controller.api.admin;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/admin/images")
@Api(value = "images", description = "Images")
public class AdminImagesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminImagesController.class);

    @Autowired
    private ImageServiceImpl imageService;

    @Autowired
    @Qualifier("retryableResizeImageService")
    private ResizeImageService resizedImageService;

    @Autowired
    private FileStore fileStore;

    @RequestMapping(value = "/view/{imageId}", method = RequestMethod.GET)
    @ApiOperation(value = "View image in original size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewPhoto(@PathVariable Long imageId, WebRequest webRequest) {
        Image image = imageService.findOne(imageId);

        return viewImage(webRequest, image);
    }

    @RequestMapping(value = "/view/{imageUUID}/uuid", method = RequestMethod.GET)
    @ApiOperation(value = "View image in original size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewPhoto(@PathVariable String imageUUID, WebRequest webRequest) {
        Image image = imageService.findByUuid(imageUUID);

        return viewImage(webRequest, image);
    }

    @RequestMapping(value = "/view/{imageId}/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View image in different size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewResizedPhoto(@PathVariable Long imageId, @PathVariable String sizeKey, WebRequest webRequest) {
        Image image = imageService.findOne(imageId);

        return viewResizedImage(sizeKey, webRequest, image);
    }

    @RequestMapping(value = "/view/{imageUUID}/uuid/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View image in different size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewResizedPhoto(@PathVariable String imageUUID, @PathVariable String sizeKey, WebRequest webRequest) {
        Image image = imageService.findByUuid(imageUUID);

        return viewResizedImage(sizeKey, webRequest, image);
    }

    private ResponseEntity<InputStreamResource> viewImage(WebRequest webRequest, Image image) {
        if (image == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return writeImageToResponse(webRequest, image.getCreatedDate(), image.getContentType(), image.getSize(), image.getStorageId());
        }
    }

    private ResponseEntity<InputStreamResource> viewResizedImage(String sizeKey, WebRequest webRequest, Image image) {
        if (image == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            try {
                ResizedImage resizedImage = resizedImageService.getResizedImage(image.getId(), sizeKey);

                return writeImageToResponse(webRequest, resizedImage.getCreatedDate(), image.getContentType(), resizedImage.getSize(), resizedImage.getStorageId());
            } catch (Exception e) {
                LOGGER.error("Admin resize image failed exception", e);
                return writeImageToResponse(webRequest, image.getCreatedDate(), image.getContentType(), image.getSize(), image.getStorageId());
            }
        }
    }

    private ResponseEntity<InputStreamResource> writeImageToResponse(WebRequest webRequest, DateTime createdDate, String contentType, Long size, String storageId) {
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
