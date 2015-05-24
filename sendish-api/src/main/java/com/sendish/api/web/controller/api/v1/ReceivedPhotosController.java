package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.LocationDto;
import com.sendish.api.dto.PhotoTraveledDto;
import com.sendish.api.dto.ReceivedPhotoDetailsDto;
import com.sendish.api.dto.ReceivedPhotoDto;
import com.sendish.api.exception.ResizeFailedException;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.ResizePhotoService;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.service.impl.PhotoVoteServiceImpl;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;
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
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/photos/received")
@Api(value = "received-photos", description = "Received photos API")
public class ReceivedPhotosController {

    @Autowired
    private PhotoServiceImpl photoService;

    @Autowired
    private PhotoVoteServiceImpl photoVoteService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private FileStore fileStore;

    @Autowired
    @Qualifier("retryableResizePhotoService")
    private ResizePhotoService resizedPhotoService;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get received photos list", notes = "This method will return the list of received photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<ReceivedPhotoDto> getReceivedPhotos(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return photoService.findAutoReceivedByUserId(user.getUserId(), page);
    }

    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get received photo details", notes = "Always try to send GPS coordinates if you open the details for the FIRST TIME ONLY!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<ReceivedPhotoDetailsDto> receivedPhotoDetails(@PathVariable Long photoId, AuthUser user,
                                                                        @ModelAttribute LocationDto location, BindingResult bindingResult) throws BindException {
        if (location.getLatitude() == null || location.getLongitude() == null) {
            if (userService.getLastLocation(user.getUserId()) == null) {
                bindingResult.rejectValue("latitude", null, "Latitude must be provided because user location cannot be determined");
                bindingResult.rejectValue("longitude", null, "Longitude must be provided because user location cannot be determined");
                throw new BindException(bindingResult);
            }
        }

        ReceivedPhotoDetailsDto photo = photoService.getReceivedByPhotoIdAndUserId(photoId, user.getUserId(), location.getLongitude(), location.getLatitude());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photo, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{photoId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete received photo")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Photo deleted"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> deleteReceived(@PathVariable Long photoId, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoService.deletePhotoReceiver(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    /**
     * @see PhotosController#traveledLocations(Long, Integer, AuthUser)
     */
    @Deprecated
    @RequestMapping(value = "/{photoId}/traveled", method = RequestMethod.GET)
    @ApiOperation(value = "Get where received photo has traveled", notes = "DEPRECATED! Use /api/v1.0/photos/{photoId}/traveled")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<List<PhotoTraveledDto>> receivedTraveled(@PathVariable Long photoId, @RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<PhotoTraveledDto> traveledLocations = photoService.getTraveledLocations(photoId, page);
            return new ResponseEntity<>(traveledLocations, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{photoUUID}/view", method = RequestMethod.GET)
    @ApiOperation(value = "View received photo in original size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewOriginalReceived(@PathVariable String photoUUID, WebRequest webRequest, AuthUser user) {
        Photo photo = photoService.findReceivedByPhotoUuid(photoUUID, user.getUserId());

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return viewPhoto(webRequest, photo.getCreatedDate(), photo.getContentType(), photo.getSize(), photo.getStorageId());
        }
    }

    @RequestMapping(value = "/{photoUUID}/view/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View received photo in different size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewReceived(@PathVariable String photoUUID, @PathVariable String sizeKey,
                                                            WebRequest webRequest, AuthUser user) {
        Photo photo = photoService.findReceivedByPhotoUuid(photoUUID, user.getUserId());

        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return returnResizedPhoto(sizeKey, webRequest, photo);
        }
    }

    @RequestMapping(value = "/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like received given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK")
    })
    public ResponseEntity<Void> like(@PathVariable Long photoId, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoVoteService.likeReceived(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike received given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK")
    })
    public ResponseEntity<Void> dislike(@PathVariable Long photoId, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoVoteService.dislikeReceived(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{photoId}/report", method = RequestMethod.PUT)
    @ApiOperation(value = "Report received given photo", notes = "Reason must be provided and reasonText is optional")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK")
    })
    public ResponseEntity<Void> report(@PathVariable Long photoId, @RequestParam String reason, @RequestParam(required = false) String reasonText, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoVoteService.reportReceived(photoId, reason, reasonText, user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
