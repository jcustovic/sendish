package com.sendish.api.web.controller.api.v1;

import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.LocationBasedFileUploadValidator;
import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.dto.PhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import com.wordnik.swagger.annotations.*;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
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
import scala.None;

import javax.validation.Valid;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/photos")
@Api(value = "photos", description = "Photos API - get details, send photos and delete photos")
public class PhotosController {

    @Autowired
    private PhotoServiceImpl photoService;

    @Autowired
    private LocationBasedFileUploadValidator locationBasedFileUploadValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(locationBasedFileUploadValidator);
    }

    @RequestMapping(value = "/received", method = RequestMethod.GET)
    @ApiOperation(value = "Get list of received photos", notes = "This method will return the list of received photos")
    @ApiResponses({
         @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> getReceivedPhotos(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return getDummyPhotos();
    }

    @RequestMapping(value = "/received/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details of received photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public PhotoDetailsDto receivedPhotoDetails(@PathVariable Long photoId, AuthUser user) {
        return new PhotoDetailsDto();
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
        return getDummyPhotos();
    }

    @RequestMapping(value = "/sent/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details", notes = "Only for the owner of the photo!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public PhotoDetailsDto details(@PathVariable Long photoId, AuthUser user) {
        return new PhotoDetailsDto();
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
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Upload new photo")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Image upload is successful and the resource is created", response = None.class),
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
    public void like(@PathVariable Long photoId) {

    }

    @RequestMapping(value = "/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike a given photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void dislike(@PathVariable Long photoId) {

    }

    @RequestMapping(value = "/{photoId}/report", method = RequestMethod.PUT)
    @ApiOperation(value = "Report a given photo", notes = "Reason must be provided and reasonText is optional")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void report(@PathVariable Long photoId, @RequestParam String reason, @RequestParam(required = false) String reasonText) {

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

    public List<PhotoDto> getDummyPhotos() {
        List<PhotoDto> dummyPhotos = new LinkedList<>();
        PrettyTime prettyTime = new PrettyTime();
        PhotoDto photo1 = new PhotoDto();
        photo1.setDescription("Cool photo");
        photo1.setCityCount(10);
        photo1.setCommentCount(4);
        photo1.setLikeCount(7);
        photo1.setImgLocation("test");
        photo1.setCity("Zagreb");
        photo1.setCountry("Croatia");
        photo1.setTimeAgo(prettyTime.format(DateTime.now().minusMinutes(30).toDate()));

        PhotoDto photo2 = new PhotoDto();
        photo2.setDescription("Cool photo no 2");
        photo2.setCityCount(20);
        photo2.setCommentCount(7);
        photo2.setLikeCount(1);
        photo2.setImgLocation("test23");
        photo2.setCity("München");
        photo2.setCountry("Germany");
        photo2.setTimeAgo(prettyTime.format(DateTime.now().minusHours(10).toDate()));

        dummyPhotos.add(photo1);
        dummyPhotos.add(photo2);

        return dummyPhotos;
    }

}
