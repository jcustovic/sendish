package com.sendish.api.service.impl;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.sendish.api.dto.FeedItemDto;
import com.sendish.api.dto.PhotoType;
import com.sendish.api.util.CityUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoComment;
import com.sendish.repository.model.jpa.PhotoReceiver;

@Service
public class FeedServiceImpl {
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private PhotoRepository photoRepository;
	
	@Autowired
	private PhotoReceiverRepository photoReceiverRepository;
	
	@Autowired
	private PhotoCommentRepository photoCommentRepository;

	@Transactional
	public List<FeedItemDto> getMyFeed(Long userId, Integer page) {
		// TODO: Redis impl
		List<FeedItemDto> feeds = new LinkedList<>();

		List<Photo> photos = photoRepository.findByUserId(userId, 
        		new PageRequest(page, 3, Direction.DESC, "createdDate"));
		
		for (Photo photo : photos) {
			List<PhotoComment> photoComments = photoCommentRepository.findByPhotoId(photo.getId(), new PageRequest(page, 3, Direction.DESC, "createdDate"));
			photoComments.stream().forEach(pc -> feeds.add(mapToPhotoCommentFeed(pc)));
			
			List<PhotoReceiver> photoReceivers = photoReceiverRepository.findByPhotoIdAndOpenedDateNotNull(photo.getId(), 
					new PageRequest(page, 3, Direction.DESC, "createdDate"));
			
			photoReceivers.stream().filter(pr -> pr.getLike() != null && pr.getLike()).forEach(pr -> feeds.add(mapToPhotoLikedFeed(pr)));
		}

		return feeds;
	}

	private FeedItemDto mapToPhotoLikedFeed(PhotoReceiver photoReceiver) {
		Photo photo = photoReceiver.getPhoto();
		
		FeedItemDto feedItem = new FeedItemDto();
		feedItem.setDescription(CityUtils.getLocationName(photo.getCity()) + " liked your photo.");
		feedItem.setPhotoId(photo.getId());
		feedItem.setPhotoType(PhotoType.SENT);
		feedItem.setPhotoUuid(photo.getUuid());
		feedItem.setTimeAgo(prettyTime.format(photoReceiver.getCreatedDate().toDate()));

		return feedItem;
	}

	private FeedItemDto mapToPhotoCommentFeed(PhotoComment photoComment) {
		Photo photo = photoComment.getPhoto();
		
		FeedItemDto feedItem = new FeedItemDto();
		feedItem.setDescription(UserUtils.getDisplayName(photoComment.getUser()) + " commented your photo.");
		feedItem.setPhotoId(photo.getId());
		feedItem.setPhotoType(PhotoType.SENT);
		feedItem.setPhotoUuid(photo.getUuid());
		feedItem.setTimeAgo(prettyTime.format(photoComment.getCreatedDate().toDate()));

		return feedItem;
	}
	
}
