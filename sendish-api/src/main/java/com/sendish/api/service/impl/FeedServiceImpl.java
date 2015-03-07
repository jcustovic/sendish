package com.sendish.api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.FeedItemDto;
import com.sendish.api.dto.PhotoType;
import com.sendish.api.util.CityUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserActivity;

@Service
public class FeedServiceImpl {
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private UserActivityServiceImpl userActivityService;

	@Transactional
	public List<FeedItemDto> getMyFeed(Long userId, Integer page) {
		List<UserActivity> activity = userActivityService.findUserActivity(userId, page);
		
		return activity.stream().map(a -> mapToFeedItemDto(a)).collect(Collectors.toList());
	}

	private FeedItemDto mapToFeedItemDto(UserActivity userActivity) {
		FeedItemDto feedItem = new FeedItemDto();
		feedItem.setDisplayName(getDisplayName(userActivity.getReferenceType(), userActivity.getFromUser()));
		feedItem.setDescription(userActivity.getText());
		feedItem.setPhotoId(Long.valueOf(userActivity.getReferenceId()));
		feedItem.setPhotoType(PhotoType.SENT);
		feedItem.setPhotoUuid(userActivity.getPhotoUuid());
		feedItem.setTimeAgo(prettyTime.format(userActivity.getCreatedDate().toDate()));

		return feedItem;
	}

	private String getDisplayName(String referenceType, User user) {
		if ("PHOTO_COMMENT".equals(referenceType)) {
			return UserUtils.getDisplayNameWithCity(user);
		}
		
		return CityUtils.getLocationName(user.getDetails().getCurrentCity());
	}
	
}
