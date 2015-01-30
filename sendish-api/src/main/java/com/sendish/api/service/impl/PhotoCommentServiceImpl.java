package com.sendish.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.sendish.api.dto.CommentDto;
import com.sendish.repository.PhotoCommentVoteRepository;
import com.sendish.repository.model.jpa.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class PhotoCommentServiceImpl {
	
	private static final int COMMENT_PAGE_SIZE = 20;
    private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private PhotoCommentRepository photoCommentRepository;
	
	@Autowired
	private PhotoRepository photoRepository;
	
	@Autowired
	private UserRepository userRepository;

    @Autowired
    private PhotoCommentVoteRepository photoCommentVoteRepository;

	public PhotoComment save(Long photoId, String comment, Long userId) {
		Photo photo = photoRepository.findOne(photoId);
		User user = userRepository.findOne(userId);
		
		PhotoComment photoComment = new PhotoComment();
		photoComment.setPhoto(photo);
		photoComment.setUser(user);
		photoComment.setComment(comment);
		// TODO: Maybe restrict to only my photos and received photos?
		// TODO: Increase comment count on photo
		
		return photoCommentRepository.save(photoComment);
	}
	
	public List<CommentDto> findByPhotoId(Long photoId, Integer page) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photoId, 
        		new PageRequest(page, COMMENT_PAGE_SIZE, Direction.DESC, "createdDate"));

        return mapToCommentDto(photoComments);
    }

    public List<CommentDto> findFirstByPhotoId(Long photoId, Integer howMany) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photoId,
                new PageRequest(0, howMany, Direction.DESC, "createdDate"));

        return mapToCommentDto(photoComments);
    }

	public void like(Long photoCommentId, Long userId) {
        voteOnComment(photoCommentId, userId, true);
	}

    public void dislike(Long photoCommentId, Long userId) {
        voteOnComment(photoCommentId, userId, false);
	}

    public PhotoComment findOne(Long photoCommentId) {
        return photoCommentRepository.findOne(photoCommentId);
    }

    private List<CommentDto> mapToCommentDto(List<PhotoComment> comments) {
        List<CommentDto> commentDtos = new ArrayList<>(comments.size());
        for (PhotoComment comment : comments) {
            CommentDto commentDto = new CommentDto();
            UserDetails userDetails = comment.getUser().getDetails();
            commentDto.setId(comment.getId());
            commentDto.setUserId(userDetails.getUserId());
            commentDto.setUserName(userDetails.getCurrentCity().getName() + ", " + userDetails.getCurrentCity().getCountry().getName());
            commentDto.setComment(comment.getComment());
            commentDto.setLikes(comment.getLikes());
            commentDto.setDislikes(comment.getDislikes());
            commentDto.setTimeAgo(prettyTime.format(comment.getCreatedDate().toDate()));

            commentDtos.add(commentDto);
        }

        return commentDtos;
    }

    private void voteOnComment(Long photoCommentId, Long userId, boolean like) {
        PhotoCommentVote photoCommentVote = photoCommentVoteRepository.findOne(new PhotoCommentVoteId(userId, photoCommentId));
        if (photoCommentVote == null) {
            // TODO: Counter on PhotoComment
            PhotoCommentVote newVote = new PhotoCommentVote();
            newVote.setLike(like);
            newVote.setUser(userRepository.findOne(userId));
            newVote.setComment(photoCommentRepository.findOne(photoCommentId));
            photoCommentVoteRepository.save(newVote);
        }
    }

}
