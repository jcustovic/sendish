package com.sendish.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sendish.api.web.device.DeviceUtils;
import com.sendish.repository.model.jpa.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.ActivityItemDto;
import com.sendish.api.redis.KeyUtils;
import com.sendish.api.util.CityUtils;
import com.sendish.api.util.StringUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.UserActivityRepository;

@Service
@Transactional
public class UserActivityServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserActivityServiceImpl.class);
	
	private static final int PAGE_SIZE = 20;
	private static final int MAX_ACTIVITY_PER_USER = 50;
    private static final int MAX_ACTIVITY_TEXT_IN_DB_LENGTH = 512;
    
    public static final String PHOTO_SENT_TYPE = "PHOTO_SENT"; // Opens sent photo details
    public static final String PHOTO_RECEIVED_TYPE = "PHOTO_RECEIVED"; // Opens received photo details
    public static final String PHOTO_SENT_COMMENT_TYPE = "PHOTO_SENT_COMMENT"; // Opens sent photo comments
    public static final String PHOTO_RECEIVED_COMMENT_TYPE = "PHOTO_RECEIVED_COMMENT"; // Opens received photo comments
    public static final String INBOX_ITEM_TYPE = "INBOX_ITEM"; // Opens inbox item details
	public static final String PHOTO_REPLY_TYPE = "PHOTO_REPLY"; // Opens photo reply details (chat)
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private UserActivityRepository userActivityRepository;
	
	@Autowired
	private StatisticsServiceImpl statisticsService;
	
	@Autowired
    private StringRedisTemplate template;
	
	public List<ActivityItemDto> getActivitites(Long userId, Integer page) {
		List<UserActivity> activity = findUserActivity(userId, page);
		if (page == 0) {
			statisticsService.markActivitiesAsRead(userId);
		}
		
		return activity.stream().map(this::mapToActivityItemDto).collect(Collectors.toList());
	}

	private ActivityItemDto mapToActivityItemDto(UserActivity userActivity) {
		ActivityItemDto activityItem = new ActivityItemDto();
        String displayName = getDisplayName(userActivity.getReferenceType(), userActivity.getFromUser());
		activityItem.setDisplayName(displayName);
		activityItem.setDescription(userActivity.getText());
		activityItem.setReferenceId(userActivity.getReferenceId());
		if (DeviceUtils.isIOSWithVersionGreatherThan("1.0") || DeviceUtils.isAndroid()) {
			activityItem.setReferenceType(getDtoReferenceTypeV1_1(userActivity.getReferenceType()));
		} else {
			activityItem.setReferenceType(getDtoReferenceType(userActivity.getReferenceType()));	
		}
		activityItem.setImageUuid(userActivity.getImageUuid());
		activityItem.setTimeAgo(prettyTime.format(userActivity.getCreatedDate().toDate()));

		return activityItem;
	}
	
	private String getDtoReferenceTypeV1_1(String referenceType) {
		if ("PHOTO_LIKED".equals(referenceType)) {
			return PHOTO_SENT_TYPE;
		} else if ("PHOTO_COMMENT".equals(referenceType) || "PHOTO_SENT_COMMENT_LIKED".equals(referenceType) 
				|| "SENT_PHOTO_REPLY_COMMENT".equals(referenceType)) {
			return PHOTO_SENT_TYPE;
		} else if ("PHOTO_RECEIVED_COMMENT_LIKED".equals(referenceType) || "RECEIVED_PHOTO_REPLY_COMMENT".equals(referenceType)) {
			return PHOTO_RECEIVED_TYPE;
		} else if ("INBOX_ITEM".equals(referenceType)) {
			return INBOX_ITEM_TYPE;
		}
		LOGGER.warn("Unknown referenceType {} so returning that name", referenceType);
		
		return "UNKNOWN";
	}

	/**
	 * Remove when all users move to iOS >= 1.1 or above!
	 * 
	 */
	@Deprecated
	private String getDtoReferenceType(String referenceType) {
		if ("PHOTO_LIKED".equals(referenceType)) {
			return PHOTO_SENT_TYPE;
		} else if ("PHOTO_COMMENT".equals(referenceType) || "PHOTO_SENT_COMMENT_LIKED".equals(referenceType) 
				|| "SENT_PHOTO_REPLY_COMMENT".equals(referenceType)) {
			return PHOTO_SENT_COMMENT_TYPE;
		} else if ("PHOTO_RECEIVED_COMMENT_LIKED".equals(referenceType) || "RECEIVED_PHOTO_REPLY_COMMENT".equals(referenceType)) {
			return PHOTO_RECEIVED_COMMENT_TYPE;
		} else if ("INBOX_ITEM".equals(referenceType)) {
			return INBOX_ITEM_TYPE;
		} else if ("NEW_PHOTO_REPLY".equals(referenceType)) {
            return PHOTO_REPLY_TYPE;
        }
		LOGGER.warn("Unknown referenceType {} so returning that name", referenceType);
		
		return "UNKNOWN";
	}

	private String getDisplayName(String referenceType, User user) {
        if (user == null) {
            // TODO: Hack here. If from user is empty (e.g. welcome inbox messages, admin messages etc.).
            return "Sendish";
        }
		if ("PHOTO_COMMENT".equals(referenceType)
				|| "RECEIVED_PHOTO_REPLY_COMMENT".equals(referenceType)
				|| "SENT_PHOTO_REPLY_COMMENT".equals(referenceType)) {
			return UserUtils.getDisplayNameWithCity(user);
		}
		
		return CityUtils.getTrimmedLocationName(user.getDetails().getCurrentCity());
	}

	public void addPhotoCommentActivity(PhotoComment photoComment) {
		Photo photo = photoComment.getPhoto();
		if (photo.getUser().isUserActive()) {
			UserActivity activity = new UserActivity();
			activity.setFromUser(photoComment.getUser());
			activity.setUser(photo.getUser());
			activity.setImageUuid(photo.getUuid());
			activity.setReferenceType("PHOTO_COMMENT");
			activity.setReferenceId(photo.getId().toString());
	        String text = StringUtils.trim(" commented on your photo: " + photoComment.getComment(), MAX_ACTIVITY_TEXT_IN_DB_LENGTH);
			activity.setText(text);
			
			activity = userActivityRepository.save(activity);
			addActivityToUserTimeline(photo.getUser().getId(), activity.getId());
		}
	}
	
	public void addReplyToPhotoCommentActivity(PhotoComment photoComment) {
        User replyToUser = photoComment.getReplyTo().getUser();
		if (replyToUser.isUserActive()) {
			Photo photo = photoComment.getPhoto();
			UserActivity activity = new UserActivity();
			activity.setFromUser(photoComment.getUser());
			activity.setUser(replyToUser);
			activity.setImageUuid(photo.getUuid());
			if (photo.getUser().getId().equals(replyToUser.getId())) {
				activity.setReferenceType("SENT_PHOTO_REPLY_COMMENT");
			} else {
				activity.setReferenceType("RECEIVED_PHOTO_REPLY_COMMENT");
			}
			activity.setReferenceId(photo.getId().toString());
	        String text = StringUtils.trim(" replied: " + photoComment.getComment(), MAX_ACTIVITY_TEXT_IN_DB_LENGTH);
			activity.setText(text);
			
			activity = userActivityRepository.save(activity);
			addActivityToUserTimeline(replyToUser.getId(), activity.getId());
		}
	}
	
	public void addPhotoLikedActivity(Photo photo, User user) {
		if (photo.getUser().isUserActive()) {
			UserActivity activity = new UserActivity();
			activity.setFromUser(user);
			activity.setUser(photo.getUser());
			activity.setImageUuid(photo.getUuid());
			activity.setReferenceType("PHOTO_LIKED");
			activity.setReferenceId(photo.getId().toString());
			activity.setText(" liked your photo");
			
			activity = userActivityRepository.save(activity);
			addActivityToUserTimeline(photo.getUser().getId(), activity.getId());
		}
	}

    public void addNewPhotoReplyActivity(PhotoReply photoReply) {
        User photoOwner = photoReply.getPhoto().getUser();
        if (photoOwner.isUserActive()) {
            UserActivity activity = new UserActivity();
            activity.setFromUser(photoReply.getUser());
            activity.setUser(photoOwner);
            activity.setImageUuid(photoReply.getUuid());
            activity.setReferenceType("NEW_PHOTO_REPLY");
            activity.setReferenceId(photoReply.getId().toString());
            activity.setText(" sent you photo reply");

            activity = userActivityRepository.save(activity);
            addActivityToUserTimeline(photoOwner.getId(), activity.getId());
        }
    }
	
	public void addCommentLikedActivity(PhotoComment comment, User user) {
		if (comment.getUser().isUserActive()) {
			User photoOwner = comment.getPhoto().getUser();
			String referenceType;
			if  (photoOwner.getId().equals(comment.getUser().getId())) {
				referenceType = "PHOTO_SENT_COMMENT_LIKED";
			} else {
				referenceType = "PHOTO_RECEIVED_COMMENT_LIKED";
			}
			
			UserActivity activity = new UserActivity();
			activity.setFromUser(user);
			activity.setUser(comment.getUser());
			activity.setImageUuid(comment.getPhoto().getUuid());
			activity.setReferenceType(referenceType);
			activity.setReferenceId(comment.getPhoto().getId().toString());
	        String text = StringUtils.trim(" liked your comment: " + comment.getComment(), MAX_ACTIVITY_TEXT_IN_DB_LENGTH);
	        activity.setText(text);
			
			activity = userActivityRepository.save(activity);
			addActivityToUserTimeline(comment.getUser().getId(), activity.getId());
		}
	}
	
	public List<UserActivity> findUserActivity(Long userId, int page) {
		List<Long> userActivities = getUserActivity(userId, page);
		
		if (userActivities.isEmpty()) {
			return new ArrayList<>(0);
		} else {
			return userActivities.stream()
					.map(userActivityRepository::findOne)
					.filter(ua -> ua != null) // TODO: Maybe log which ID were not found.
					.collect(Collectors.toList());
		}
	}
	
	private List<Long> getUserActivity(Long userId, int page) {
		long start = PAGE_SIZE * page;
		long end = PAGE_SIZE * (page + 1) - 1;
		List<String> activityIds = userTimeline(userId).range(start, end);
		
		return activityIds.stream().map(Long::valueOf).collect(Collectors.toList());
	}

	private void addActivityToUserTimeline(Long userId, Long activityId) {
		BoundListOperations<String, String> userTimeline = userTimeline(userId);
		userTimeline.leftPush(activityId.toString());
		userTimeline.trim(0, MAX_ACTIVITY_PER_USER - 1);
		statisticsService.setNewActivityFlag(userId);
	}
	
	private BoundListOperations<String, String> userTimeline(Long userId) {
    	return template.boundListOps(KeyUtils.userTimeline(userId));
    }

}
