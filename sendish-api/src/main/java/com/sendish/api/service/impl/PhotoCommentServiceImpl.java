package com.sendish.api.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sendish.api.dto.CommentDto;
import com.sendish.api.notification.AsyncNotificationProvider;
import com.sendish.api.redis.dto.CommentStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.PhotoCommentVoteRepository;
import com.sendish.repository.model.jpa.*;

import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;

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
    
    @Autowired
    private AsyncNotificationProvider notificationProvider;
    
    @Autowired
    private RankingServiceImpl rankingService;
    
    @Autowired
    private UserActivityServiceImpl userActivityService;

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
        if (!photo.getDeleted() && !photo.getUser().getId().equals(userId)) {
        	sendCommentNotificationToPhotoOwner(user, photo, comment);
        	userActivityService.addPhotoCommentActivity(photoComment);
        }

		return photoComment;
	}
	
	public List<CommentDto> findByPhotoId(Long photoId, Long userId, int page) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photoId, 
        		new PageRequest(page, COMMENT_PAGE_SIZE, Direction.DESC, "createdDate"));

        return mapToCommentDto(photoComments, userId);
    }

    public List<CommentDto> findFirstByPhotoId(Long photoId, Long userId, int howMany) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photoId,
                new PageRequest(0, howMany, Direction.DESC, "createdDate"));

        return mapToCommentDto(photoComments, userId);
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

    private List<CommentDto> mapToCommentDto(List<PhotoComment> comments, Long userId) {
        return comments.stream().map(comment -> mapToCommentDto(comment, userId)).collect(Collectors.toList());
    }
    
    private void sendCommentNotificationToPhotoOwner(User user, Photo photo, String comment) {
        if (user.getDetails().getReceiveNotifications()) {
        	Map<String, Object> newCommentFields = new HashMap<>();
            newCommentFields.put("TYPE", "NEW_COMMENT");
            newCommentFields.put("REFERENCE_ID", photo.getId());
            
            String notText = getNotificationText(user, comment);
            
        	notificationProvider.sendPlainTextNotification(notText, newCommentFields, photo.getUser().getId());	
        }
	}

	private String getNotificationText(User user, String comment) {
		String prefix = UserUtils.getDisplayNameWithCity(user) + " said: ";
		int charsLeftForComment = 50 - prefix.length();
		
		if (comment.length() > charsLeftForComment) {
			return prefix + StringUtils.left(comment, charsLeftForComment - 3) + "...";
		} else {
			return prefix + comment;
		}
	}

    private CommentDto mapToCommentDto(PhotoComment comment, Long userId) {
        CommentDto commentDto = new CommentDto();
        UserDetails userDetails = comment.getUser().getDetails();
        commentDto.setId(comment.getId());
        commentDto.setUserId(userDetails.getUserId());
        commentDto.setUserName(UserUtils.getDisplayNameWithCity(comment.getUser()));
        commentDto.setComment(comment.getComment());
        commentDto.setTimeAgo(prettyTime.format(comment.getCreatedDate().toDate()));
        
        // TODO: Can we somehow save a round trip to the database for each comment?
        PhotoCommentVote photoCommentVote = photoCommentVoteRepository.findOne(new PhotoCommentVoteId(userId, comment.getId()));
        if (photoCommentVote != null) {
        	commentDto.setLiked(photoCommentVote.getLike());
        }

        // TODO: Maybe get from database when we will store it there so we save on trip to Redis.
        CommentStatisticsDto commentStatistics = statisticsRepository.getCommentStatistics(comment.getId());
        commentDto.setLikes(commentStatistics.getLikeCount());
        commentDto.setDislikes(commentStatistics.getDislikeCount());

        return commentDto;
    }

    private void voteOnComment(Long photoCommentId, Long userId, boolean like) {
        PhotoCommentVote photoCommentVote = photoCommentVoteRepository.findOne(new PhotoCommentVoteId(userId, photoCommentId));
        if (photoCommentVote == null) {
        	User user = userRepository.findOne(userId);
            // TODO: Maybe allow vote change?
        	PhotoComment comment = photoCommentRepository.findOne(photoCommentId);
            PhotoCommentVote newVote = new PhotoCommentVote();
            newVote.setLike(like);
            newVote.setUser(user);
            newVote.setComment(comment);
            photoCommentVoteRepository.save(newVote);

            if (like) {
                statisticsRepository.likeComment(photoCommentId);
                rankingService.addPointsForLikedComment(comment.getUser().getId());
                
                if (!comment.getPhoto().getDeleted()) {
                	userActivityService.addCommentLikedActivity(comment, user);
                }
            } else {
                statisticsRepository.dislikeComment(photoCommentId);
                rankingService.removePointsForDislikedComment(comment.getUser().getId());
            }
        }
    }

}
