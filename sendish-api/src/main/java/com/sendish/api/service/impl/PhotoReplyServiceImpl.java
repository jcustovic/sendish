package com.sendish.api.service.impl;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sendish.api.dto.ChatMessageDto;
import com.sendish.api.dto.ChatThreadDetailsDto;
import com.sendish.api.dto.NewPhotoReplyMessageDto;
import com.sendish.api.dto.PhotoReplyDto;
import com.sendish.api.dto.PhotoReplyFileUpload;
import com.sendish.api.store.FileStore;
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
		chatService.createChatForPhotoReply(photoReply);

		statisticsService.incrementPhotoReplyWihtPhotoCount(photoReplyFileUpload.getPhotoId());
		statisticsService.setNewPhotoReplyActivity(photoReply.getPhoto().getUser().getId());
		
		// TODO: Send push notification

		return photoReply;
	}

	public PhotoReply findByUserIdAndPhotoId(Long userId, Long photoId) {
		return photoReplyRepository.findByUserIdAndPhotoId(userId, photoId);
	}

	public PhotoReply findByUuid(String photoReplyUUID) {
		return photoReplyRepository.findByUuid(photoReplyUUID);
	}
	
	public ChatThread findChatThreadByPhotoReplyId(Long photoReplyId) {
		return chatService.findThreadByPhotoReplyId(photoReplyId);
	}

	public ChatThreadDetailsDto findChatThreadWithFirstPageByPhotoReplyId(Long photoReplyId, Long userId) {
		ChatThread chatThread = chatService.findThreadByPhotoReplyId(photoReplyId);
		if (chatThread == null) {
			return null;	
		}
		ChatThreadDetailsDto chatThreadDto = new ChatThreadDetailsDto();
		chatThreadDto.setId(chatThread.getId());
		// TODO: Get name from ChatTreadUser
		// chatThreadDto.setName();
		// TODO: Add image
		List<ChatMessageDto> messages = chatService.findByThreadId(chatThread.getId(), 0);
		chatThreadDto.setMessages(messages);
		
		return chatThreadDto;
	}

	public List<PhotoReplyDto> findAll(Long userId, Integer page) {
		List<ChatThread> photoReplyChatThreads = chatThreadRepository.findPhotoReplyThreadsByUserId(userId,
				new PageRequest(page, PHOTO_REPLIES_PAGE_SIZE, Direction.DESC, "chatThread.lastActivity"));

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

		Long userId = newMessage.getUserId();
		ChatMessageDto chatMessageDto = chatService.newChatMessage(chatThread.getId(), userId, newMessage.getMessage());
		
		PhotoReply photoReply = chatThread.getPhotoReply();
		if (photoReply.getUser().getId().equals(userId)) {
			statisticsService.setNewPhotoReplyActivity(photoReply.getPhoto().getUser().getId());
		} else {
			statisticsService.setNewPhotoReplyActivity(photoReply.getUser().getId());
		}
		
		// TODO: Send push notification
		
		return chatMessageDto;
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
		photoReplyDto.setLastActivity(prettyTime.format(chatThread.getLastActivity().toDate()));
		photoReplyDto.setId(photoReply.getId());
		photoReplyDto.setImgUuid(photoReply.getUuid());
		if (photoReply.getUser().getId().equals(userId)) {
			photoReplyDto.setReceived(false);
			photoReplyDto.setUsername(UserUtils.getDisplayName(photoReply.getPhoto().getUser()));
		} else {
			photoReplyDto.setReceived(true);
			photoReplyDto.setUsername(UserUtils.getDisplayName(photoReply.getUser()));
		}
		
		return photoReplyDto;
	}

}
