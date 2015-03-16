package com.sendish.api.web.controller.api.v1;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.sendish.api.dto.HotPhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.HotPhotoServiceImpl;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.service.impl.ResizedPhotoServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.repository.model.jpa.HotPhoto;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.ResizedPhoto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/hot-photos")
@Api(value = "hot-photos", description = "Hot Photos API - get list")
public class HotPhotosController {
	
	@Autowired
	private HotPhotoServiceImpl hotPhotoService;
	
	@Autowired
    private PhotoServiceImpl photoService;
	
	@Autowired
    private ResizedPhotoServiceImpl resizedPhotoService;

    @Autowired
    private FileStore fileStore;
	
	@RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get hot photo list")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> list(@RequestParam(defaultValue = "0") Integer page) {
        return hotPhotoService.findAllActive(page);
    }
	
	@RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<HotPhotoDetailsDto> photoDetails(@PathVariable Long photoId, AuthUser user) throws BindException {
		HotPhotoDetailsDto photo = hotPhotoService.findByPhotoIdForUser(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photo, HttpStatus.OK);
        }
    }
	
    @RequestMapping(value = "/{photoUUID}/view", method = RequestMethod.GET)
    @ApiOperation(value = "View hot photo in original size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewOriginal(@PathVariable String photoUUID, WebRequest webRequest) {
        HotPhoto hotPhoto = hotPhotoService.findPhotoByPhotoUuid(photoUUID);

        if (hotPhoto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
        	Photo photo = hotPhoto.getPhoto();
            return viewPhoto(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
        }
    }

    @RequestMapping(value = "/{photoUUID}/view/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View hot photo in different size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> view(@PathVariable String photoUUID, @PathVariable String sizeKey, 
    		WebRequest webRequest) {
        HotPhoto photo = hotPhotoService.findPhotoByPhotoUuid(photoUUID);

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            ResizedPhoto resizedPhoto = resizedPhotoService.getResizedPhoto(photo.getPhotoId(), sizeKey);

			return viewPhoto(webRequest, resizedPhoto.getCreatedDate(), photo.getPhoto().getContentType(), 
					resizedPhoto.getSize(), resizedPhoto.getStorageId());
        }
    }

    @RequestMapping(value = "/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like given hot photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "You are trying to vote on your photo"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> like(@PathVariable Long photoId, AuthUser user) {
    	HotPhoto photo = hotPhotoService.findByPhotoId(photoId);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photo.getPhoto().getUser().getId().equals(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            photoService.likePhoto(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike given hot photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "You are trying to vote on your photo"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> dislike(@PathVariable Long photoId, AuthUser user) {
    	HotPhoto photo = hotPhotoService.findByPhotoId(photoId);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photo.getPhoto().getUser().getId().equals(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            photoService.dislikePhoto(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
    
    private ResponseEntity<InputStreamResource> viewPhoto(WebRequest webRequest, DateTime createdDate, String contentType, Long size, String storageId) {
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
