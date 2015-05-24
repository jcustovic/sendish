package com.sendish.api.web.controller.api.v1;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import com.sendish.api.dto.*;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.web.controller.validator.ReportPhotoReplyValidator;
import com.sendish.repository.model.jpa.Photo;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.PhotoReplyServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.web.controller.validator.NewPhotoReplyMessageValidator;
import com.sendish.api.web.controller.validator.PhotoReplyFileUploadValidator;
import com.sendish.repository.model.jpa.ChatThread;
import com.sendish.repository.model.jpa.PhotoReply;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1.0/photo-replies")
@Api(value = "photo-replies", description = "Reply with photo to received photo")
public class PhotoReplyController {
	
	@Autowired
    private PhotoReplyServiceImpl photoReplyService;
	
	@Autowired
	private FileStore fileStore;
	
	@Autowired
	private PhotoServiceImpl photoService;

	@Autowired
	private PhotoReplyFileUploadValidator photoReplyFileUploadValidator;
	
	@Autowired
	private NewPhotoReplyMessageValidator newPhotoReplyMessageValidator;

	@Autowired
	private ReportPhotoReplyValidator reportPhotoReplyValidator;
	
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
	
	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation(value = "List of all photo replies", notes = "All received and sent photo replies orderd by last activity")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "OK")
    })
	public List<PhotoReplyDto> findAll(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
		return photoReplyService.findAll(user.getUserId(), page);
	}

	@RequestMapping(value = "/photo/{photoId}/photo-replies", method = RequestMethod.GET)
	@ApiOperation(value = "List of all photo replies on a photo", notes = "Only photo owner can see all the replies")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Photo not found")
	})
	public ResponseEntity<List<PhotoReplyDto>> findByPhoto(@PathVariable Long photoId, @RequestParam(defaultValue = "0") Integer page, AuthUser user) {
		Photo photo = photoService.findByIdAndUserId(photoId, user.getUserId());
		if (photo == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			List<PhotoReplyDto> photoReplies = photoReplyService.findByPhotoId(photoId, user.getUserId(), page);

			return new ResponseEntity<>(photoReplies, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/{photoReplyId}", method = RequestMethod.GET)
	@ApiOperation(value = "Chat details with messages for photo reply")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "OK"),
    	@ApiResponse(code = 404, message = "Photo reply not found")
    })
	public ResponseEntity<ChatThreadDetailsDto> getChatThreadForPhotoReply(@PathVariable Long photoReplyId, AuthUser user) {
		ChatThreadDetailsDto chatThread = photoReplyService.findChatThreadWithFirstPageByPhotoReplyIdAndUserId(photoReplyId, user.getUserId());
		if (chatThread == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
            chatThread.getMessages().stream().forEach(this::mapPhotoUrl);

			return new ResponseEntity<>(chatThread, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/{photoReplyId}", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete photo reply")
    @ApiResponses({
    	@ApiResponse(code = 204, message = "Deleted"),
    	@ApiResponse(code = 404, message = "Photo reply not found")
    })
	public ResponseEntity<ChatThreadDetailsDto> delete(@PathVariable Long photoReplyId, AuthUser user) {
		if (photoReplyService.removeUserFromChatThread(photoReplyId, user.getUserId())) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value = "/{photoReplyId}/messages", method = RequestMethod.GET)
	@ApiOperation(value = "Get messages for photo reply", notes = "NOTE: In photo reply details you already get the first page!")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "OK"),
    	@ApiResponse(code = 404, message = "Photo reply not found")
    })
	public ResponseEntity<List<ChatMessageDto>> getChatMessagesForPhotoReply(@PathVariable Long photoReplyId, @RequestParam(defaultValue = "0") Integer page,
			AuthUser user) {
		ChatThread chatThread = photoReplyService.findThreadByPhotoReplyIdAndUserId(photoReplyId, user.getUserId());
		if (chatThread == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			List<ChatMessageDto> msgs = photoReplyService.findChatMessagesByChatThreadId(chatThread.getId(), page);
            msgs.stream().forEach(this::mapPhotoUrl);

			return new ResponseEntity<>(msgs, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/new-messages", method = RequestMethod.POST)
    @ApiOperation(value = "Post new message")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "OK"),
    	@ApiResponse(code = 400, message = "Validation errors")
    })
	public ResponseEntity<ChatMessageDto> postNewMessage(@RequestBody @Valid NewPhotoReplyMessageDto newMessage, BindingResult result, AuthUser user) throws BindException {
		newMessage.setUserId(user.getUserId());
		newPhotoReplyMessageValidator.validate(newMessage, result);
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		
		ChatMessageDto chatMessageDto = photoReplyService.newMessage(newMessage);
		mapPhotoUrl(chatMessageDto);
    	
        return new ResponseEntity<>(chatMessageDto, HttpStatus.OK);
    }

	@RequestMapping(value = "/report", method = RequestMethod.POST)
	@ApiOperation(value = "Report photo reply")
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Validation errors")
	})
	public ResponseEntity<Void> report(@RequestBody @Valid ReportPhotoReplyDto reportDto, BindingResult result, AuthUser user) throws BindException {
		reportDto.setUserId(user.getUserId());
		reportPhotoReplyValidator.validate(reportDto, result);
		if (result.hasErrors()) {
			throw new BindException(result);
		}

		photoReplyService.report(reportDto);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(value = "/{photoReplyUUID}/view", method = RequestMethod.GET)
    @ApiOperation(value = "View photo reply")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 403, message = "Not reply or photo owner"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewPhotoReply(@PathVariable String photoReplyUUID, WebRequest webRequest, AuthUser user) {
        PhotoReply photoReply = photoReplyService.findByUuid(photoReplyUUID);

        if (photoReply == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photoReply.getUser().getId().equals(user.getUserId()) || photoReply.getPhoto().getUser().getId().equals(user.getUserId())) {
        	return viewPhotoReply(webRequest, photoReply.getCreatedDate(), photoReply.getContentType(), photoReply.getSize(), photoReply.getStorageId());
        } else {
        	return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
	
	@RequestMapping(value = "/{photoReplyUUID}/view/{sizeKey}", method = RequestMethod.GET)
    @ApiOperation(value = "View photo reply in different size")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 403, message = "Not reply or photo owner"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InputStreamResource> viewPhotoReply(@PathVariable String photoReplyUUID, @PathVariable String sizeKey, 
    		WebRequest webRequest, AuthUser user) {
		// TODO: If needed return resized photo and call different service method
		return viewPhotoReply(photoReplyUUID, webRequest, user);
	}
	
	private ResponseEntity<InputStreamResource> viewPhotoReply(WebRequest webRequest, DateTime createdDate, String contentType, Long size, String storageId) {
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

    private void mapPhotoUrl(ChatMessageDto chatMessageDto) {
        if (chatMessageDto.getType().equals(ChatMessageDto.ChatMessageDtoType.IMG)) {
        	ChatMessageImageDto img = chatMessageDto.getImage();
            switch ( img.getType() ) {
                case IMAGE_PHOTO:
                    img.setRelativePath(UriComponentsBuilder.fromPath("/api/v1.0/photos/{photoUUID}/view")
                            .buildAndExpand(img.getUuid())
                            .toUriString());
                    break;
                case IMAGE_PHOTO_REPLY:
                	img.setRelativePath(UriComponentsBuilder.fromPath("/api/v1.0/photo-replies/{photoReplyUUID}/view")
                            .buildAndExpand(img.getUuid())
                            .toUriString());
                    break;
				default:
					break;
            }
        }
    }

}
