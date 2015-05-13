package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.*;
import com.sendish.api.exception.ResizeFailedException;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.ResizePhotoService;
import com.sendish.api.service.impl.PhotoVoteServiceImpl;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.LocationBasedFileUploadValidator;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.ResizedPhoto;
import com.wordnik.swagger.annotations.*;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/photos")
@Api(value = "photos", description = "Photos API - get details, send photos and delete photos")
public class PhotosController {

    @Autowired
    private PhotoServiceImpl photoService;

    @Autowired
    private PhotoVoteServiceImpl photoVoteService;

    @Autowired
    private LocationBasedFileUploadValidator locationBasedFileUploadValidator;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    @Qualifier("retryableResizePhotoService")
    private ResizePhotoService resizedPhotoService;

    @Autowired
    private FileStore fileStore;

    @InitBinder("locationBasedFileUpload")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(locationBasedFileUploadValidator);
    }

    @RequestMapping(value = "/{photoUuid}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<PhotoDetailsDto> findOne(@PathVariable String photoUuid, AuthUser user) {
        PhotoDetailsDto photoDetails = photoService.getByUuid(photoUuid, user.getUserId());
        if (photoDetails == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photoDetails, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK"),
        @ApiResponse(code = 400, message = "You are trying to vote on your photo"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> likePhoto(@PathVariable String photoUuid, AuthUser user) {
        Photo photo = photoService.findByUuid(photoUuid);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photo.getUser().getId().equals(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            photoVoteService.likePhoto(photo.getId(), user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{photoUuid}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK"),
        @ApiResponse(code = 400, message = "You are trying to vote on your photo"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> dislikePhoto(@PathVariable String photoUuid, AuthUser user) {
        Photo photo = photoService.findByUuid(photoUuid);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photo.getUser().getId().equals(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            photoVoteService.dislikePhoto(photo.getId(), user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{photoUuid}/view", method = RequestMethod.GET)
    @ApiOperation(value = "View photo in original size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewOriginal(@PathVariable String photoUuid, WebRequest webRequest) {
        Photo photo = photoService.findByUuid(photoUuid);

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return viewPhoto(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
        }
    }

    @RequestMapping(value = "/{photoUuid}/view/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View photo in different size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> view(@PathVariable String photoUuid, @PathVariable String sizeKey,
                                                    WebRequest webRequest) {
        Photo photo = photoService.findByUuid(photoUuid);

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return returnResizedPhoto(sizeKey, webRequest, photo);
        }
    }

    @RequestMapping(value = "/nearby/{cityId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photos nearby")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> findPhotosNearby(@PathVariable Long cityId, @RequestParam(defaultValue = "0") Integer page) {
        return photoService.findNearbyByCity(cityId, page);
    }

    @RequestMapping(value = "/sendish-upload", method = RequestMethod.POST)
    @ApiOperation(value = "Upload new photo", notes = "If all si OK and you get code 201 check Location header to point you to the newly created photo", response = Void.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 201 will be returned"),
        @ApiResponse(code = 201, message = "Image upload is successful and the resource is created"),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class),
        @ApiResponse(code = 429, message = "Upload limit exceeded")
    })
    public ResponseEntity<Void> upload(@Valid @ModelAttribute LocationBasedFileUpload locationBasedFileUpload,
                                       MultipartFile image, AuthUser user) { // FIXME: MultipartFile image is also specified here because of swagger!
        if (userService.isSentLimitReached(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        PhotoSendingDetails photoSendingDetails = photoService.processNewPhoto(locationBasedFileUpload, user.getUserId());

        // TODO: Maybe return also the locations where the image was sent if available!
        final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/v1.0/photos/{id}").build()
                .expand(photoSendingDetails.getPhotoId()).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    // ########### Sent Photos services ###########

    @RequestMapping(value = "/sent", method = RequestMethod.GET)
    @ApiOperation(value = "Get sent photos list", notes = "This method will return the list of sent photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> getSentPhotos(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return photoService.findByUserId(user.getUserId(), page);
    }

    @RequestMapping(value = "/sent/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get sent photo details", notes = "Only for the owner of the photo!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<CommonPhotoDetailsDto> details(@PathVariable Long photoId, AuthUser user) {
        CommonPhotoDetailsDto photo = photoService.getDetailsByIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photo, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/sent/{photoId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete sent photo")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Photo deleted"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> deleteSent(@PathVariable Long photoId, AuthUser user) {
        Photo photo = photoService.findByIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoService.deletePhoto(photoId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/sent/{photoId}/traveled", method = RequestMethod.GET)
    @ApiOperation(value = "Get where the sent photo has traveled")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<List<PhotoTraveledDto>> sentTraveled(@PathVariable Long photoId, @RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        Photo photo = photoService.findByIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<PhotoTraveledDto> traveledLocations = photoService.getTraveledLocations(photoId, page);
            return new ResponseEntity<>(traveledLocations, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/sent/{photoUUID}/view", method = RequestMethod.GET)
    @ApiOperation(value = "View sent photo in original size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewOriginalSent(@PathVariable String photoUUID, WebRequest webRequest, AuthUser user) {
        Photo photo = photoService.findByUserIdAndUuid(user.getUserId(), photoUUID);

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return viewPhoto(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
        }
    }
    
    @RequestMapping(value = "/sent/{photoUUID}/view/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View sent photo in different size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewSent(@PathVariable String photoUUID, @PathVariable String sizeKey, 
    		WebRequest webRequest, AuthUser user) {
    	Photo photo = photoService.findByUserIdAndUuid(user.getUserId(), photoUUID);

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return returnResizedPhoto(sizeKey, webRequest, photo);
        }
    }
    
    private ResponseEntity<InputStreamResource> returnResizedPhoto(String sizeKey, WebRequest webRequest, Photo photo) {
		try {
			ResizedPhoto resizedPhoto = resizedPhotoService.getResizedPhoto(photo.getId(), sizeKey);
			
			return viewPhoto(webRequest, resizedPhoto.getCreatedDate(), photo.getContentType(), resizedPhoto.getSize(), resizedPhoto.getStorageId());
		} catch (ResizeFailedException e) {
			return viewPhoto(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
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
