package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.*;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.LocationBasedFileUploadValidator;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import com.wordnik.swagger.annotations.*;
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
import java.util.ArrayList;
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

    @InitBinder("upload")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(locationBasedFileUploadValidator);
    }

    @RequestMapping(value = "/received", method = RequestMethod.GET)
    @ApiOperation(value = "Get list of received photos", notes = "This method will return the list of received photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<ReceivedPhotoDto> getReceivedPhotos(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return photoService.findReceivedByUserId(user.getUserId(), page);
    }

    @RequestMapping(value = "/received/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details of received photo", notes = "Always try to send GPS coordinates if you open the details for the FIRST TIME ONLY!")
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

        ReceivedPhotoDetailsDto photo = photoService.findReceivedByIdAndUserId(photoId, location.getLongitude(), location.getLatitude(), user.getUserId());
        if (photo == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photo, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/received/{photoId}/traveled", method = RequestMethod.GET)
    @ApiOperation(value = "Get where received photo has traveled")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoTraveledDto> receivedTraveled(@PathVariable Long photoId, AuthUser user) {
        // TODO: Implement me
        return new ArrayList<>();
    }

    @RequestMapping(value = "/received/{photoUUID}/download", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> receivedDownload(@PathVariable String photoUUID, WebRequest webRequest, AuthUser user) {
        Photo photo = photoService.findReceivedByUuid(photoUUID, user.getUserId());

        return download(webRequest, photo);
    }

    @RequestMapping(value = "/sent", method = RequestMethod.GET)
    @ApiOperation(value = "Get list of sent photos", notes = "This method will return the list of sent photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> getSentPhotos(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return photoService.findByUserId(user.getUserId(), page);
    }

    @RequestMapping(value = "/sent/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details", notes = "Only for the owner of the photo!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<PhotoDetailsDto> details(@PathVariable Long photoId, AuthUser user) {
        PhotoDetailsDto photo = photoService.findByIdAndUserId(photoId, user.getUserId());
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(photo, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/sent/{photoId}/traveled", method = RequestMethod.GET)
    @ApiOperation(value = "Get where the sent photo has traveled")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoTraveledDto> sentTraveled(@PathVariable Long photoId, AuthUser user) {
        // TODO: Implement me
        return new ArrayList<>();
    }

    @RequestMapping(value = "/sent/{photoUUID}/download", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> sentDownload(@PathVariable String photoUUID, WebRequest webRequest, AuthUser user) {
        Photo photo = photoService.findByUserIdAndUuid(user.getUserId(), photoUUID);

        return download(webRequest, photo);
    }

    @RequestMapping(value = "/sendish-upload", method = RequestMethod.POST)
    @ApiOperation(value = "Upload new photo", notes = "If all si OK and you get code 201 check Location header to point you to the newly created photo", response = Void.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 201 will be returned"),
        @ApiResponse(code = 201, message = "Image upload is successful and the resource is created"),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public ResponseEntity<Void> upload(@Valid @ModelAttribute LocationBasedFileUpload upload, MultipartFile image, AuthUser user) { // FIXME: MultipartFile image is also specified here because of swagger!
        Long photoId = photoService.saveNewImage(upload, user.getUserId());

        final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/v1.0/photos/{id}").build()
                .expand(photoId).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like a given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void like(@PathVariable Long photoId, AuthUser user) {
        photoService.like(photoId, user.getUserId());
    }

    @RequestMapping(value = "/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike a given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void dislike(@PathVariable Long photoId, AuthUser user) {
        photoService.dislike(photoId, user.getUserId());
    }

    @RequestMapping(value = "/{photoId}/report", method = RequestMethod.PUT)
    @ApiOperation(value = "Report a given photo", notes = "Reason must be provided and reasonText is optional")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void report(@PathVariable Long photoId, @RequestParam String reason, @RequestParam(required = false) String reasonText, AuthUser user) {
        photoService.report(photoId, reason, reasonText, user.getUserId());
    }

    private ResponseEntity<InputStreamResource> download(WebRequest webRequest, Photo photo) {
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (webRequest.checkNotModified(photo.getCreatedDate().getMillis())) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(photo.getContentType()));
        headers.setContentLength(photo.getSize());

        try {
            InputStreamResource isr = new InputStreamResource(photoService.getPhotoContent(photo.getStorageId()));
            return new ResponseEntity<>(isr, headers, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
