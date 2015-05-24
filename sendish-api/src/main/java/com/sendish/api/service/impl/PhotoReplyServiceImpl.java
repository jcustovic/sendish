package com.sendish.api.service.impl;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sendish.api.dto.*;
import com.sendish.push.notification.AsyncNotificationProvider;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.User;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sendish.api.store.FileStore;
import com.sendish.api.util.CityUtils;
import com.sendish.api.util.ImageUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.ChatThreadRepository;
import com.sendish.repository.PhotoReplyRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.ChatThread;
import com.sendish.repository.model.jpa.PhotoReply;

@Service
@Transactional
public class PhotoReplyServiceImpl {
	
	private static final int PHOTO_REPLIES_PAGE_SIZE = 20;
	
	private static PrettyTime prettyTime = new PrettyTime();

	@Autowired
	private PhotoReplyRepository photoReplyRepository;

	@Autowired
	private FileStore fileStore;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PhotoRepository photoRepository;
	
	@Autowired
	private ChatServiceImpl chatService;
	
	@Autowired
	private ChatThreadRepository chatThreadRepository;
	
	@Autowired
	private StatisticsServiceImpl statisticsService;

	@Autowired
	private AsyncNotificationProvider notificationProvider;

	@Autowired
	private UserActivityServiceImpl userActivityService;

    @Autowired
    private PhotoVoteServiceImpl photoVoteService;

	public PhotoReply processNew(PhotoReplyFileUpload photoReplyFileUpload) {
		MultipartFile file = photoReplyFileUpload.getImage();
		PhotoReply photoReply = mapToPhotoReply(photoReplyFileUpload, file);

		Dimension dimension;
		try {
			dimension = ImageUtils.getDimension(file.getInputStream());
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read image dimensions for file " + file.getName());
		}
		photoReply.setWidth((int) dimension.getWidth());
		photoReply.setHeight((int) dimension.getHeight());

		String fileStoreId;
		try {
			fileStoreId = fileStore.save(file.getInputStream(), "reply_photo_original");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		photoReply.setStorageId(fileStoreId);

		photoReply = photoReplyRepository.save(photoReply);
        ChatThread chatThread = chatService.createChatForPhotoReply(photoReply);

        // Insert first 2 messages - original photo and reply photo
        Photo originalPhoto = photoReply.getPhoto();
        chatService.newPhotoImageChatMessage(chatThread.getId(), originalPhoto.getUser().getId(), originalPhoto.getUuid(), originalPhoto.getDescription());
        chatService.newPhotoReplyImageChatMessage(chatThread.getId(), photoReply.getUser().getId(), photoReply.getUuid(), photoReply.getDescription());

        photoVoteService.likePhoto(photoReply.getPhoto().getId(), photoReply.getUser().getId());

		Long photoOwnerId = photoReply.getPhoto().getUser().getId();

		statisticsService.incrementPhotoReplyWihtPhotoCount(photoReplyFileUpload.getPhotoId());
		statisticsService.setNewPhotoReplyActivity(photoOwnerId);

		User sender = photoReply.getUser();
		String text = UserUtils.getDisplayNameWithCity(sender) + " replied with photo";
		sendPhotoReplyNewsNotification(photoOwnerId, text, photoReply);

		userActivityService.addNewPhotoReplyActivity(photoReply);

        return photoReply;
	}

	public PhotoReply findByUserIdAndPhotoId(Long userId, Long photoId) {
		return photoReplyRepository.findByUserIdAndPhotoId(userId, photoId);
	}

	public PhotoReply findByUuid(String photoReplyUUID) {
		return photoReplyRepository.findByUuid(photoReplyUUID);
	}
	
	public ChatThread findThreadByPhotoReplyIdAndUserId(Long photoReplyId, Long userId) {
		return chatService.findThreadByPhotoReplyIdAndUserId(photoReplyId, userId);
	}

	public ChatThreadDetailsDto findChatThreadWithFirstPageByPhotoReplyIdAndUserId(Long photoReplyId, Long userId) {
		ChatThread chatThread = chatService.findThreadByPhotoReplyIdAndUserId(photoReplyId, userId);
		if (chatThread == null) {
			return null;	
		}
		PhotoReply photoReply = chatThread.getPhotoReply();
		Long photoReplyOwnerId = photoReply.getUser().getId();
		String photoOwnerName = CityUtils.getTrimmedLocationName(photoReply.getPhoto().getCity());
		String photoReplyOwnerName = CityUtils.getTrimmedLocationName(photoReply.getUser().getDetails().getCurrentCity());
		
		ChatThreadDetailsDto chatThreadDto = new ChatThreadDetailsDto();
		chatThreadDto.setId(chatThread.getId());
		if (photoReplyOwnerId.equals(userId)) {
			chatThreadDto.setName(photoOwnerName);
		} else {
			chatThreadDto.setName(photoReplyOwnerName);			
		}
		List<ChatMessageDto> messages = chatService.findByThreadId(chatThread.getId(), 0);
		messages.stream().forEach(m -> addDisplayName(m, photoReplyOwnerId, photoOwnerName, photoReplyOwnerName));

		chatThreadDto.setMessages(messages);
		
		return chatThreadDto;
	}

    private void addDisplayName(ChatMessageDto message, Long photoReplyOwnerId, String photoOwnerName, String photoReplyOwnerName) {
    	if (message.getUserId().equals(photoReplyOwnerId)) {
    		message.setDisplayName(photoReplyOwnerName);
    	} else {
    		message.setDisplayName(photoOwnerName);
    	}
	}

	public List<PhotoReplyDto> findAll(Long userId, Integer page) {
		List<ChatThread> photoReplyChatThreads = chatThreadRepository.findPhotoReplyThreadsByUserId(userId,
                new PageRequest(page, PHOTO_REPLIES_PAGE_SIZE, Direction.DESC, "chatThread.lastActivity"));
        if (page == 0) {
            statisticsService.markPhotoReplyActivitiesAsRead(userId);
        }

		return photoReplyChatThreads.stream()
				.map(chatThread -> mapToPhotoReplyDto(chatThread, userId))
				.collect(Collectors.toList());
	}
	
	public boolean removeUserFromChatThread(Long photoReplyId, Long userId) {
		ChatThread chatThread = chatService.findThreadByPhotoReplyId(photoReplyId);
		if (chatThread == null) {
			return false;
		}

		return chatService.removeUserFromChatThread(chatThread.getId(), userId);
	}
	
	public PhotoReply findOne(Long photoReplyId) {
		return photoReplyRepository.findOne(photoReplyId);
	}
	
	public ChatMessageDto newMessage(NewPhotoReplyMessageDto newMessage) {
		ChatThread chatThread = chatService.findThreadByPhotoReplyId(newMessage.getPhotoReplyId());

		User sender = userRepository.findOne(newMessage.getUserId());
		ChatMessageDto chatMessageDto = chatService.newTextChatMessage(chatThread.getId(), sender.getId(), newMessage.getMessage());
		
		PhotoReply photoReply = chatThread.getPhotoReply();
		Long userReceivingReplyId;
		if (photoReply.getUser().equals(sender)) {
			userReceivingReplyId = photoReply.getPhoto().getUser().getId();
		} else {
			userReceivingReplyId = photoReply.getUser().getId();
		}
		statisticsService.setNewPhotoReplyActivity(userReceivingReplyId);
        String text = UserUtils.getDisplayName(sender) + " replied";
		sendPhotoReplyNewsNotification(userReceivingReplyId, text, photoReply);
		
		return chatMessageDto;
	}

	public void report(ReportPhotoReplyDto reportDto) {
		PhotoReply photoReply = findOne(reportDto.getPhotoReplyId());
		photoReply.setDeleted(true);
		photoReply.setReportedBy(userRepository.findOne(reportDto.getUserId()));
		photoReply.setReportType(reportDto.getReportType());
		photoReply.setReportText(reportDto.getReportText());

		if (photoReply.getUser().getId().equals(reportDto.getUserId())) {
			statisticsService.reportUser(photoReply.getPhoto().getUser().getId());
		} else {
			statisticsService.reportUser(photoReply.getUser().getId());
		}

		photoReplyRepository.save(photoReply);
	}

	public List<PhotoReplyDto> findByPhotoId(Long photoId, Long userId, Integer page) {
		List<ChatThread> photoReplyChats = chatThreadRepository.findByPhotoReplyPhotoId(photoId,
                new PageRequest(page, PHOTO_REPLIES_PAGE_SIZE, Direction.DESC, "lastActivity"));

		return photoReplyChats.stream()
				.map(chatThread -> mapToPhotoReplyDto(chatThread, userId))
				.collect(Collectors.toList());
	}
	
	private PhotoReply mapToPhotoReply(PhotoReplyFileUpload photoReplyFileUpload, MultipartFile file) {
		PhotoReply photoReply = new PhotoReply();
		photoReply.setUser(userRepository.findOne(photoReplyFileUpload.getUserId()));
		photoReply.setPhoto(photoRepository.findOne(photoReplyFileUpload.getPhotoId()));
		photoReply.setName(file.getName());
		photoReply.setContentType(file.getContentType());
		photoReply.setSize(file.getSize());
		photoReply.setDescription(photoReplyFileUpload.getDescription());
		photoReply.setUuid(UUID.randomUUID().toString());

		return photoReply;
	}
	
	private PhotoReplyDto mapToPhotoReplyDto(ChatThread chatThread, Long userId) {
		PhotoReply photoReply = chatThread.getPhotoReply();
		PhotoReplyDto photoReplyDto = new PhotoReplyDto();
        photoReplyDto.setTimeAgo(prettyTime.format(chatThread.getLastActivity().toDate()));
        photoReplyDto.setId(photoReply.getId());
		photoReplyDto.setImageUuid(photoReply.getUuid());
		if (photoReply.getUser().getId().equals(userId)) {
			photoReplyDto.setReceived(false);
			photoReplyDto.setDisplayName(CityUtils.getLocationName(photoReply.getPhoto().getCity()));
			//photoReplyDto.setMessage("received your photo reply");
		} else {
			photoReplyDto.setReceived(true);
			photoReplyDto.setDisplayName(CityUtils.getLocationName(photoReply.getUser().getDetails().getCurrentCity()));
			//photoReplyDto.setDisplayName(UserUtils.getDisplayNameWithCity(photoReply.getUser()));
			//photoReplyDto.setMessage("replied with photo");
		}
		photoReplyDto.setMessage("");
		
		return photoReplyDto;
	}

	private void sendPhotoReplyNewsNotification(Long receivingUserId, String text, PhotoReply photoReply) {
		Map<String, Object> photoReplyNotifFields = new HashMap<>();
		photoReplyNotifFields.put("TYPE", "OPEN_PHOTO_REPLY");
		photoReplyNotifFields.put("REFERENCE_ID", photoReply.getId());

		notificationProvider.sendPlainTextNotification(text, photoReplyNotifFields, receivingUserId);
	}

}
