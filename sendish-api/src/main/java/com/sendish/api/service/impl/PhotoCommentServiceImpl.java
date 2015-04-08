package com.sendish.api.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.CommentDto;
import com.sendish.api.dto.NewCommentDto;
import com.sendish.api.notification.AsyncNotificationProvider;
import com.sendish.api.redis.dto.CommentStatisticsDto;
import com.sendish.api.util.StringUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoCommentVoteRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoComment;
import com.sendish.repository.model.jpa.PhotoCommentVote;
import com.sendish.repository.model.jpa.PhotoCommentVoteId;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserDetails;

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
    private StatisticsServiceImpl statisticsService;
    
    @Autowired
    private AsyncNotificationProvider notificationProvider;
    
    @Autowired
    private RankingServiceImpl rankingService;
    
    @Autowired
    private UserActivityServiceImpl userActivityService;

    @Deprecated
	public PhotoComment save(Long photoId, String comment, Long userId) {
        // TODO: Maybe restrict only to my photos or received photos?
		Photo photo = photoRepository.findOne(photoId);
		User user = userRepository.findOne(userId);
		
		PhotoComment photoComment = new PhotoComment();
		photoComment.setPhoto(photo);
		photoComment.setUser(user);
		photoComment.setComment(comment);

        photoComment = photoCommentRepository.save(photoComment);
        statisticsService.incrementPhotoCommentCount(photoId);
        if (!photo.getDeleted() && !photo.getUser().getId().equals(userId)) {
        	sendCommentNotificationToPhotoOwner(user, photo, comment);
        	userActivityService.addPhotoCommentActivity(photoComment);
        }

		return photoComment;
	}
	
	public PhotoComment save(NewCommentDto commentDto) {
		 // TODO: Maybe restrict only to my photos or received photos?
		Photo photo = photoRepository.findOne(commentDto.getPhotoId());
		User user = userRepository.findOne(commentDto.getUserId());
		
		PhotoComment photoComment = new PhotoComment();
		photoComment.setPhoto(photo);
		photoComment.setUser(user);
		photoComment.setComment(commentDto.getComment());
		if (commentDto.getReplyToId() != null) {
			PhotoComment replyToComment = photoCommentRepository.findOne(commentDto.getReplyToId());
			photoComment.setReplyTo(replyToComment);	
			photoComment.setParent(replyToComment.getParent() == null ? replyToComment : replyToComment.getParent());
		}

        photoComment = photoCommentRepository.save(photoComment);
        
        statisticsService.incrementPhotoCommentCount(commentDto.getPhotoId());
        if (photoComment.getReplyTo() == null 
        		&& !photo.getDeleted() 
        		&& !photo.getUser().getId().equals(commentDto.getUserId())) {
        	sendCommentNotificationToPhotoOwner(user, photo, commentDto.getComment());
        	userActivityService.addPhotoCommentActivity(photoComment);
        } else if (photoComment.getReplyTo() != null) {
        	sendReplyToCommentNotification(photoComment);
        	userActivityService.addReplyToPhotoCommentActivity(photoComment);
        }

		return photoComment;
	}
	
	public List<CommentDto> findByPhotoId(Long photoId, Long userId, int page) {
		// TODO: Query DSL optimization?
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
        
        statisticsService.decrementPhotoCommentCount(photoComment.getPhoto().getId());
    }

    private List<CommentDto> mapToCommentDto(List<PhotoComment> comments, Long userId) {
        return comments.stream().map(comment -> mapToCommentDto(comment, userId)).collect(Collectors.toList());
    }
    
    private void sendCommentNotificationToPhotoOwner(User user, Photo photo, String comment) {
        if (user.getDetails().getReceiveCommentNotifications()) {
        	Map<String, Object> newCommentFields = new HashMap<>();
            newCommentFields.put("TYPE", "OPEN_SENT_PHOTO");
            newCommentFields.put("REFERENCE_ID", photo.getId());
            
            String notText = getNewCommentNotificationText(user, comment);
            
        	notificationProvider.sendPlainTextNotification(notText, newCommentFields, photo.getUser().getId());	
        }
	}
    
    private void sendReplyToCommentNotification(PhotoComment photoComment) {
    	User replyToUser = photoComment.getReplyTo().getUser();
    	if (replyToUser.getDetails().getReceiveCommentNotifications()) {
        	Map<String, Object> newCommentFields = new HashMap<>();
        	if (photoComment.getPhoto().getUser().getId().equals(replyToUser.getId())) {
        		newCommentFields.put("TYPE", "OPEN_SENT_PHOTO");	
        	} else {
        		newCommentFields.put("TYPE", "OPEN_RECEIVED_PHOTO");	
        	}
            newCommentFields.put("REFERENCE_ID", photoComment.getPhoto().getId());
            
            String notText = getReplyOnCommentNotificationText(photoComment.getUser(), photoComment.getComment());
            
        	notificationProvider.sendPlainTextNotification(notText, newCommentFields, replyToUser.getId());
        }
	}

	private String getNewCommentNotificationText(User user, String comment) {
		String text = UserUtils.getDisplayNameWithCity(user) + " said: " + comment;

        return StringUtils.trim(text, 50, "...");
	}
	
	private String getReplyOnCommentNotificationText(User user, String comment) {
		String text = UserUtils.getDisplayNameWithCity(user) + " replied: " + comment;

        return StringUtils.trim(text, 50, "...");
	}

    private CommentDto mapToCommentDto(PhotoComment comment, Long userId) {
        CommentDto commentDto = new CommentDto();
        UserDetails userDetails = comment.getUser().getDetails();
        commentDto.setId(comment.getId());
        commentDto.setUserId(userDetails.getUserId());
        commentDto.setUserName(UserUtils.getDisplayNameWithCity(comment.getUser()));
        commentDto.setComment(comment.getComment());
        commentDto.setTimeAgo(prettyTime.format(comment.getCreatedDate().toDate()));
        if (comment.getReplyTo() != null) {
        	commentDto.setReplyUsername(UserUtils.getDisplayName(comment.getReplyTo().getUser()));
        }
        boolean isPhotoOwner = comment.getPhoto().getUser().getId().equals(userId);
        boolean isCommentOwner = comment.getUser().getId().equals(userId);
        commentDto.setCanDelete(isCommentOwner || isPhotoOwner);
        
        // TODO: Can we somehow save a round trip to the database for each comment?
        PhotoCommentVote photoCommentVote = photoCommentVoteRepository.findOne(new PhotoCommentVoteId(userId, comment.getId()));
        if (photoCommentVote != null) {
        	commentDto.setLiked(photoCommentVote.getLike());
        }

        // TODO: Maybe get from database (since we have it on comment object) when we will store it there so we save on trip to Redis.
        CommentStatisticsDto commentStatistics = statisticsService.getCommentStatistics(comment.getId());
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
                statisticsService.likeComment(photoCommentId);
                rankingService.addPointsForLikedComment(comment.getUser());
                
                if (!comment.getPhoto().getDeleted()) {
                	userActivityService.addCommentLikedActivity(comment, user);
                }
            } else {
                statisticsService.dislikeComment(photoCommentId);
                rankingService.removePointsForDislikedComment(comment.getUser());
            }
        }
    }

}
