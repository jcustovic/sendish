package com.sendish.api.web.controller.api.admin;

import com.sendish.api.exception.ResizeFailedException;
import com.sendish.api.service.ResizePhotoService;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.ResizedPhoto;
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
@RequestMapping("/api/admin/photos")
@Api(value = "photos", description = "Photos")
public class AdminPhotosController {

    @Autowired
    private PhotoServiceImpl photoService;

    @Autowired
    @Qualifier("retryableResizePhotoService")
    private ResizePhotoService resizedPhotoService;

    @Autowired
    private FileStore fileStore;

    @RequestMapping(value = "/view/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "View photo in original size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewPhoto(@PathVariable Long photoId, WebRequest webRequest) {
        Photo photo = photoService.findOne(photoId);

        return viewPhoto(webRequest, photo);
    }


    @RequestMapping(value = "/view/{photoUUID}/uuid", method = RequestMethod.GET)
    @ApiOperation(value = "View photo in original size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewPhoto(@PathVariable String photoUUID, WebRequest webRequest) {
        Photo photo = photoService.findByUuid(photoUUID);

        return viewPhoto(webRequest, photo);
    }

    @RequestMapping(value = "/view/{photoId}/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View photo in different size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewResizedPhoto(@PathVariable Long photoId, @PathVariable String sizeKey, WebRequest webRequest) {
        Photo photo = photoService.findOne(photoId);

        return viewResizedPhoto(sizeKey, webRequest, photo);
    }

    @RequestMapping(value = "/view/{photoUUID}/uuid/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View photo in different size")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewResizedPhoto(@PathVariable String photoUUID, @PathVariable String sizeKey, WebRequest webRequest) {
        Photo photo = photoService.findByUuid(photoUUID);

        return viewResizedPhoto(sizeKey, webRequest, photo);
    }

    private ResponseEntity<InputStreamResource> viewPhoto(WebRequest webRequest, Photo photo) {
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return writeImageToResponse(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
        }
    }

    private ResponseEntity<InputStreamResource> viewResizedPhoto(String sizeKey, WebRequest webRequest, Photo photo) {
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            try {
                ResizedPhoto resizedPhoto = resizedPhotoService.getResizedPhoto(photo.getId(), sizeKey);

                return writeImageToResponse(webRequest, resizedPhoto.getCreatedDate(), photo.getContentType(), resizedPhoto.getSize(), resizedPhoto.getStorageId());
            } catch (ResizeFailedException e) {
                return writeImageToResponse(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
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
