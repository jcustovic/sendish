package com.sendish.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.redis.KeyUtils;
import com.sendish.repository.UserActivityRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoComment;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserActivity;

@Service
@Transactional
public class UserActivityServiceImpl {
	
	private static final int PAGE_SIZE = 20;
	
	@Autowired
	private UserActivityRepository userActivityRepository;
	
	@Autowired
    private StringRedisTemplate template;

	public void addPhotoCommentActivity(PhotoComment photoComment) {
		Photo photo = photoComment.getPhoto();
		UserActivity activity = new UserActivity();
		activity.setFromUser(photoComment.getUser());
		activity.setUser(photo.getUser());
		activity.setPhotoUuid(photo.getUuid());
		activity.setReferenceType("PHOTO_COMMENT");
		activity.setReferenceId(photo.getId().toString());
		activity.setText(" commented your photo");
		
		activity = userActivityRepository.save(activity);
		addActivityToUserTimeline(photo.getUser().getId(), activity.getId());
	}
	
	public void addPhotoLikedActivity(Photo photo, User user) {
		UserActivity activity = new UserActivity();
		activity.setFromUser(user);
		activity.setUser(photo.getUser());
		activity.setPhotoUuid(photo.getUuid());
		activity.setReferenceType("PHOTO_LIKED");
		activity.setReferenceId(photo.getId().toString());
		activity.setText(" liked your photo");
		
		activity = userActivityRepository.save(activity);
		addActivityToUserTimeline(photo.getUser().getId(), activity.getId());
	}
	
	public List<UserActivity> findUserActivity(Long userId, int page) {
		List<Long> userActivities = getUserActivity(userId, page);
		
		if (userActivities.isEmpty()) {
			return new ArrayList<>(0);
		} else {
			return userActivities.stream().map(id -> userActivityRepository.findOne(id)).collect(Collectors.toList());
		}
	}
	
	private List<Long> getUserActivity(Long userId, int page) {
		long start = PAGE_SIZE * page;
		long end = PAGE_SIZE * (page + 1) - 1;
		List<String> activityIds = userTimeline(userId).range(start, end);
		
		return activityIds.stream().map(id -> Long.valueOf(id)).collect(Collectors.toList());
	}

	private void addActivityToUserTimeline(Long userId, Long activityId) {
		BoundListOperations<String, String> userTimeline = userTimeline(userId);
		userTimeline.leftPush(activityId.toString());
		userTimeline.trim(0, 49);
	}
	
	private BoundListOperations<String, String> userTimeline(Long userId) {
    	return template.boundListOps(KeyUtils.userTimeline(userId));
    }

}
