package com.sendish.api.web.controller.api.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sendish.api.dto.CommentDto;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.PhotoCommentServiceImpl;
import com.sendish.repository.model.jpa.PhotoComment;
import com.sendish.repository.model.jpa.UserDetails;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/photo-comments")
@Api(value = "photo-comments", description = "Comments on photo")
public class PhotoCommentsController {
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private PhotoCommentServiceImpl photoCommentService;

    @Autowired
    private PhotoServiceImpl photoService;

    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get comments for a specific photo")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<CommentDto> list(@PathVariable Long photoId, @RequestParam(defaultValue = "0") Integer page) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    	final List<PhotoComment> comments = photoCommentService.findByPhotoId(photoId, page);
    	
        return mapToCommentDto(comments);
    }
    
	@RequestMapping(value = "/{photoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Get comments for a specific photo", notes = "If all si OK and you get code 201 check Location header to point you to the newly created photo")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "NOT USED! 201 will be returned", response = Void.class),
        @ApiResponse(code = 201, message = "Comment created")
    })
    public ResponseEntity<Void> newComment(@PathVariable Long photoId, @RequestParam String comment, AuthUser user) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PhotoComment photoComment = photoCommentService.save(photoId, comment, user.getUserId());
    	
    	final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/v1.0/photo-comments/{id}").build()
                .expand(photoComment.getId()).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like a comment")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void like(@PathVariable Long photoId, AuthUser user) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    	photoCommentService.like(photoId, user.getUserId());
    }

    @RequestMapping(value = "/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike a comment")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public void dislike(@PathVariable Long photoId, AuthUser user) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    	photoCommentService.dislike(photoId, user.getUserId());
    }
    
    private List<CommentDto> mapToCommentDto(List<PhotoComment> comments) {
    	List<CommentDto> commentDtos = new ArrayList<>();
    	for (PhotoComment comment : comments) {
    		CommentDto commentDto = new CommentDto();
    		UserDetails userDetails = comment.getUser().getDetails();
    		commentDto.setUserName(userDetails.getCurrentCity().getName() + ", " + userDetails.getCurrentCity().getCountry().getName());
    		commentDto.setComment(comment.getComment());
    		commentDto.setLikes(comment.getLikes());
    		commentDto.setDislikes(comment.getDislikes());
    		commentDto.setTimeAgo(prettyTime.format(comment.getCreatedDate().toDate()));
    	}
    	
		return commentDtos;
	}

}
