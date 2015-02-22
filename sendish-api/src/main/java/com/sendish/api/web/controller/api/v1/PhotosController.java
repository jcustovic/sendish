package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.*;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.ResizedPhotoServiceImpl;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.LocationBasedFileUploadValidator;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.ResizedPhoto;
import com.wordnik.swagger.annotations.*;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
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
    private LocationBasedFileUploadValidator locationBasedFileUploadValidator;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ResizedPhotoServiceImpl resizedPhotoService;

    @Autowired
    private FileStore fileStore;

    @InitBinder("locationBasedFileUpload")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(locationBasedFileUploadValidator);
    }

    @RequestMapping(value = "/received", method = RequestMethod.GET)
    @ApiOperation(value = "Get received photos list", notes = "This method will return the list of received photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<ReceivedPhotoDto> getReceivedPhotos(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return photoService.findReceivedByUserId(user.getUserId(), page);
    }

    @RequestMapping(value = "/received/{photoId}", method = RequestMethod.GET)
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

        ReceivedPhotoDetailsDto photo = photoService.openReceivedByPhotoIdAndUserId(photoId, user.getUserId(), location.getLongitude(), location.getLatitude());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photo, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/received/{photoId}", method = RequestMethod.DELETE)
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

    @RequestMapping(value = "/received/{photoId}/traveled", method = RequestMethod.GET)
    @ApiOperation(value = "Get where received photo has traveled")
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

    @RequestMapping(value = "/received/{photoUUID}/view", method = RequestMethod.GET)
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

    @RequestMapping(value = "/received/{photoUUID}/view/{sizeKey}", method = RequestMethod.GET)
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
            ResizedPhoto resizedPhoto = resizedPhotoService.getResizedPhoto(photo.getId(), sizeKey);

            return viewPhoto(webRequest, resizedPhoto.getCreatedDate(), photo.getContentType(), resizedPhoto.getSize(), resizedPhoto.getStorageId());
        }
    }

    @RequestMapping(value = "/received/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like received given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public ResponseEntity<Void> like(@PathVariable Long photoId, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoService.like(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/received/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike received given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public ResponseEntity<Void> dislike(@PathVariable Long photoId, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoService.dislike(photoId, user.getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/received/{photoId}/report", method = RequestMethod.PUT)
    @ApiOperation(value = "Report received given photo", notes = "Reason must be provided and reasonText is optional")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public ResponseEntity<Void> report(@PathVariable Long photoId, @RequestParam String reason, @RequestParam(required = false) String reasonText, AuthUser user) {
        PhotoReceiver photo = photoService.findReceivedByPhotoIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            photoService.report(photoId, reason, reasonText, user.getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

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
    public ResponseEntity<PhotoDetailsDto> details(@PathVariable Long photoId, AuthUser user) {
        PhotoDetailsDto photo = photoService.findByIdAndUserId(photoId, user.getUserId());
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
        PhotoDetailsDto photo = photoService.findByIdAndUserId(photoId, user.getUserId());
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
        PhotoDetailsDto photo = photoService.findByIdAndUserId(photoId, user.getUserId());
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
            ResizedPhoto resizedPhoto = resizedPhotoService.getResizedPhoto(photo.getId(), sizeKey);

            return viewPhoto(webRequest, resizedPhoto.getCreatedDate(), photo.getContentType(), resizedPhoto.getSize(), resizedPhoto.getStorageId());
        }
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
        if (userService.getSentLimitLeft(user.getUserId()) <= 0) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        PhotoSendingDetails photoSendingDetails = photoService.processNewImage(locationBasedFileUpload, user.getUserId());

        // TODO: Maybe return also the locations where the image was sent if available!
        final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/v1.0/photos/{id}").build()
                .expand(photoSendingDetails.getPhotoId()).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
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
