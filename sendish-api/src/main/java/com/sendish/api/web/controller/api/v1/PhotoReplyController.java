package com.sendish.api.web.controller.api.v1;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sendish.api.dto.PhotoReplyFileUpload;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.PhotoReplyServiceImpl;
import com.sendish.api.web.controller.validator.PhotoReplyFileUploadValidator;
import com.sendish.repository.model.jpa.PhotoReply;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/photo-replies")
@Api(value = "photo-replies", description = "Reply with photo to received photo")
public class PhotoReplyController {
	
	@Autowired
    private PhotoReplyServiceImpl photoReplyService;
	
	@Autowired
    private PhotoReplyFileUploadValidator photoReplyFileUploadValidator;
	
	@RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Reply with photo", notes = "If all si OK and you get code 201 check Location header to point you to the newly created photo comment")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "NOT USED! 201 will be returned"),
        @ApiResponse(code = 201, message = "Photo reply created")
    })
	public ResponseEntity<Void> postNewPhotoReply(@ModelAttribute @Valid PhotoReplyFileUpload photoReplyFileUpload, BindingResult result, 
			MultipartFile image, AuthUser user) throws BindException { // FIXME: MultipartFile image is also specified here because of swagger!
		photoReplyFileUpload.setUserId(user.getUserId());
		photoReplyFileUploadValidator.validate(photoReplyFileUpload, result);
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		
		PhotoReply photoReply = photoReplyService.processNew(photoReplyFileUpload);
    	
    	final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/v1.0/photo-replies/chat/{id}").build()
                .expand(photoReply.getId()).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
	
	@RequestMapping(value = "/chat/{photoReplyId}",method = RequestMethod.GET)
	public ResponseEntity<?> getChatForPhotoReply(@PathVariable Long photoReplyId, AuthUser user) {
		// TODO: Implement
		return null;
	}

}
