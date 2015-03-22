package com.sendish.api.web.controller.api.admin;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.dto.admin.AutoSendingPhotoDto;
import com.sendish.api.service.admin.imp.AutoSendingPhotoServiceImpl;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.LocationBasedFileUploadValidator;
import com.sendish.repository.model.jpa.AutoSendingPhoto;
import com.sendish.repository.model.jpa.Photo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/admin/auto-sending-photos")
@Api(value = "auto-sending-photos", description = "Manage automatic photo sending")
public class AdminAutoSendingPhotosController {
	
	@Autowired
	private AutoSendingPhotoServiceImpl autoSendingPhotoService;
	
	@Autowired
    private LocationBasedFileUploadValidator locationBasedFileUploadValidator;
	
	@InitBinder("locationBasedFileUpload")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(locationBasedFileUploadValidator);
    }
	
	@RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get all auto sending photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<AutoSendingPhotoDto> list(@RequestParam(defaultValue = "0") Integer page) {
        return autoSendingPhotoService.findAll(page);
    }
	
	@RequestMapping(value = "/photos", method = RequestMethod.GET)
    @ApiOperation(value = "Get all auto sending photos")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> findAllPosible(@RequestParam(defaultValue = "0") Integer page) {
        return autoSendingPhotoService.findAllAvailablePhotos(page);
    }
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ApiOperation(value = "Upload new photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 201 will be returned"),
        @ApiResponse(code = 201, message = "Image upload is successful and the resource is created"),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public ResponseEntity<Void> upload(@Valid @ModelAttribute LocationBasedFileUpload locationBasedFileUpload,
                                       MultipartFile image) { // FIXME: MultipartFile image is also specified here because of swagger!
        autoSendingPhotoService.processNewPhoto(locationBasedFileUpload);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
	
	@RequestMapping(value = "/add/photo/{photoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Add uploaded photo to auto sending list")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Added"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> addToAutoSending(@PathVariable Long photoId) {
        Photo photo = autoSendingPhotoService.findPhotoById(photoId);
        if (photo == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        autoSendingPhotoService.addNewPhoto(photoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
	
	@RequestMapping(value = "/deactivate/{autoSendingPhotoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Deactivate auto sending photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Deactivated"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> deactivate(@PathVariable Long autoSendingPhotoId) {
		AutoSendingPhoto autoSendingPhoto = autoSendingPhotoService.findOne(autoSendingPhotoId);
        if (autoSendingPhoto == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        autoSendingPhotoService.deactivate(autoSendingPhotoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
	
	@RequestMapping(value = "/activate/{autoSendingPhotoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Deactivate auto sending photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Activated"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> activate(@PathVariable Long autoSendingPhotoId) {
		AutoSendingPhoto autoSendingPhoto = autoSendingPhotoService.findOne(autoSendingPhotoId);
        if (autoSendingPhoto == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        autoSendingPhotoService.activate(autoSendingPhotoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
