package com.sendish.api.web.controller.api.v1;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sendish.api.dto.CommentDto;
import com.sendish.api.dto.NewCommentDto;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.PhotoCommentServiceImpl;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.web.controller.validator.NewCommentValidator;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoComment;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/photo-comments")
@Api(value = "photo-comments", description = "Comments on photo")
public class PhotoCommentsController {
	
	@Autowired
	private PhotoCommentServiceImpl photoCommentService;

    @Autowired
    private PhotoServiceImpl photoService;
    
    @Autowired
    private NewCommentValidator newCommentValidator;
    
    @RequestMapping(value = "/{photoId}/comments", method = RequestMethod.GET)
    @ApiOperation(value = "Get comments for a specific photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<List<CommentDto>> list(@PathVariable Long photoId, @RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<CommentDto> comments = photoCommentService.findByPhotoId(photoId, user.getUserId(), page);

        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
    
	@RequestMapping(value = "/new/{photoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Post a comment to specific photo", notes = "If all si OK and you get code 201 check Location header to point you to the newly created comment")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "NOT USED! 201 will be returned"),
        @ApiResponse(code = 201, message = "Comment created"),
    	@ApiResponse(code = 400, message = "Validation errors or bad request"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
	public ResponseEntity<Void> postNewComment(@PathVariable Long photoId,
			@RequestBody @Valid NewCommentDto newCommentDto, BindingResult result, AuthUser user) throws BindException {
		newCommentDto.setUserId(user.getUserId());
		newCommentDto.setPhotoId(photoId);
		newCommentValidator.validate(newCommentDto, result);
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		
        PhotoComment photoComment = photoCommentService.save(newCommentDto);
    	
    	final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/v1.0/photo-comments/{id}").build()
                .expand(photoComment.getId()).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
	
    @RequestMapping(value = "/comment/{photoCommentId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete your comments or any comment on a photo that you own")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Comment deleted"),
        @ApiResponse(code = 404, message = "Comment not found or you are not the owner of the comment or photo")
    })
    public ResponseEntity<Void> delete(@PathVariable Long photoCommentId, AuthUser user) {
        PhotoComment photoComment = photoCommentService.findNotDeletedById(photoCommentId);
        if (photoComment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Long photoOwnerId = photoComment.getPhoto().getUser().getId();
        Long commentOwnerId = photoComment.getUser().getId();
        if (photoOwnerId.equals(user.getUserId()) || commentOwnerId.equals(user.getUserId())) {
            photoCommentService.delete(photoCommentId);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @RequestMapping(value = "/comment/{photoCommentId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like a comment")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK"),
        @ApiResponse(code = 400, message = "You vote on your own comment or validation error (bad request)")
    })
    public ResponseEntity<Void> like(@PathVariable Long photoCommentId, AuthUser user) {
        PhotoComment photoComment = photoCommentService.findNotDeletedById(photoCommentId);
        if (photoComment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photoComment.getUser().getId().equals(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        photoCommentService.like(photoCommentId, user.getUserId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/comment/{photoCommentId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike a comment")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "OK"),
        @ApiResponse(code = 400, message = "You vote on your own comment or validation error (bad request)")
    })
    public ResponseEntity<Void> dislike(@PathVariable Long photoCommentId, AuthUser user) {
        PhotoComment photoComment = photoCommentService.findNotDeletedById(photoCommentId);
        if (photoComment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (photoComment.getUser().getId().equals(user.getUserId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    	photoCommentService.dislike(photoCommentId, user.getUserId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
