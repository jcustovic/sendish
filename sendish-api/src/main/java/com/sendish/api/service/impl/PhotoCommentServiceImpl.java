package com.sendish.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.sendish.api.dto.CommentDto;
import com.sendish.api.redis.dto.CommentStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
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

    @Autowired
    private RedisStatisticsRepository statisticsRepository;

	public PhotoComment save(Long photoId, String comment, Long userId) {
        // TODO: Maybe restrict only to my photos or received photos?
		Photo photo = photoRepository.findOne(photoId);
		User user = userRepository.findOne(userId);
		
		PhotoComment photoComment = new PhotoComment();
		photoComment.setPhoto(photo);
		photoComment.setUser(user);
		photoComment.setComment(comment);

        photoComment = photoCommentRepository.save(photoComment);
        statisticsRepository.increasePhotoCommentCount(photoId);

		return photoComment;
	}
	
	public List<CommentDto> findByPhotoId(Long photoId, int page) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photoId, 
        		new PageRequest(page, COMMENT_PAGE_SIZE, Direction.DESC, "createdDate"));

        return mapToCommentDto(photoComments);
    }

    public List<CommentDto> findFirstByPhotoId(Long photoId, int howMany) {
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

    public void delete(Long photoCommentId) {
        PhotoComment photoComment = photoCommentRepository.findOne(photoCommentId);
        photoComment.setDeleted(true);
        photoCommentRepository.save(photoComment);
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
            commentDto.setTimeAgo(prettyTime.format(comment.getCreatedDate().toDate()));

            // TODO: Maybe get from database when we will store it there so we save on trip to Redis.
            CommentStatisticsDto commentStatistics = statisticsRepository.getCommentStatistics(comment.getId());
            commentDto.setLikes(commentStatistics.getLikeCount());
            commentDto.setDislikes(commentStatistics.getDislikeCount());

            commentDtos.add(commentDto);
        }

        return commentDtos;
    }

    private void voteOnComment(Long photoCommentId, Long userId, boolean like) {
        PhotoCommentVote photoCommentVote = photoCommentVoteRepository.findOne(new PhotoCommentVoteId(userId, photoCommentId));
        if (photoCommentVote == null) {
            // TODO: Maybe allow vote change?
            PhotoCommentVote newVote = new PhotoCommentVote();
            newVote.setLike(like);
            newVote.setUser(userRepository.findOne(userId));
            newVote.setComment(photoCommentRepository.findOne(photoCommentId));
            photoCommentVoteRepository.save(newVote);

            if (like) {
                statisticsRepository.likeComment(photoCommentId);
            } else {
                statisticsRepository.dislikeComment(photoCommentId);
            }
        }
    }

}
