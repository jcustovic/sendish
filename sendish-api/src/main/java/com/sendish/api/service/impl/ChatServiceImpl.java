package com.sendish.api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.sendish.repository.model.jpa.*;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.ChatMessageDto;
import com.sendish.api.dto.ChatMessageImageDto;
import com.sendish.api.dto.ChatMessageDto.ChatMessageDtoType;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.ChatMessageRepository;
import com.sendish.repository.ChatThreadRepository;
import com.sendish.repository.ChatThreadUserRepository;
import com.sendish.repository.UserRepository;

@Service
@Transactional
public class ChatServiceImpl {
	
	private static final int CHAT_MESSAGE_PAGE_SIZE = 20;
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private ChatThreadRepository chatThreadRepository;
	
	@Autowired
	private ChatThreadUserRepository chatThreadUserRepository;
	
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	public ChatThread createChatForPhotoReply(PhotoReply photoReply) {
		// Create thread
		ChatThread chatThread = new ChatThread();
		chatThread.setPhotoReply(photoReply);
		chatThread = chatThreadRepository.save(chatThread);
		
		// Add users to chat thread
		ChatThreadUser chatThreadPhotoOwner = new ChatThreadUser();
		chatThreadPhotoOwner.setChatThread(chatThread);
		chatThreadPhotoOwner.setUser(photoReply.getPhoto().getUser());
		chatThreadUserRepository.save(chatThreadPhotoOwner);
		
		ChatThreadUser photoReplyUser = new ChatThreadUser();
		photoReplyUser.setChatThread(chatThread);
		photoReplyUser.setUser(photoReply.getUser());
		chatThreadUserRepository.save(photoReplyUser);
		
		return chatThread;
	}

	public ChatThread findThreadByPhotoReplyId(Long photoReplyId) {
		return chatThreadRepository.findByPhotoReplyId(photoReplyId);
	}
	
	public List<ChatMessageDto> findByThreadId(Long chatThreadId, Integer page) {
		List<ChatMessage> messages = chatMessageRepository.findByChatThreadId(chatThreadId, 
				new PageRequest(page, CHAT_MESSAGE_PAGE_SIZE, Direction.DESC, "createdDate"));
		
		return messages.stream().map(this::mapToMessageDto).collect(Collectors.toList());
	}
	
	public boolean removeUserFromChatThread(Long chatThreadId, Long userId) {
		ChatThreadUserId id = new ChatThreadUserId(userId, chatThreadId);
		ChatThreadUser chatUser = chatThreadUserRepository.findOne(id);
		if (chatUser == null) {
			return false;
		} else {
			chatThreadUserRepository.delete(chatUser);
			return true;
		}
	}
	
	public ChatMessageDto newTextChatMessage(Long chatThreadId, Long userId, String text) {
        return newChatMessage(chatThreadId, userId, ChatMessageType.TEXT, null, text);
	}

    public ChatMessageDto newPhotoImageChatMessage(Long chatThreadId, Long userId, String uuid, String description) {
        return newChatMessage(chatThreadId, userId, ChatMessageType.IMAGE_PHOTO, uuid, description);
    }

    public ChatMessageDto newPhotoReplyImageChatMessage(Long chatThreadId, Long userId, String uuid, String description) {
        return newChatMessage(chatThreadId, userId, ChatMessageType.IMAGE_PHOTO_REPLY, uuid, description);
    }

    private ChatMessageDto newChatMessage(Long chatThreadId, Long userId, ChatMessageType type, String imageUuid, String text) {
        ChatThread chatThread = chatThreadRepository.findOne(chatThreadId);
        chatThread.setLastActivity(DateTime.now());
        chatThreadRepository.save(chatThread);

        ChatMessage message = new ChatMessage();
        message.setChatThread(chatThread);
        message.setUser(userRepository.findOne(userId));
        message.setType(type);
        message.setImageUuid(imageUuid);
        message.setText(text);

        return mapToMessageDto(chatMessageRepository.save(message));
    }
	
	private ChatMessageDto mapToMessageDto(ChatMessage message) {
		ChatMessageDto dto = new ChatMessageDto();
		dto.setId(message.getId());
        if (message.getType().equals(ChatMessageType.TEXT)) {
            dto.setType(ChatMessageDtoType.TEXT);
        } else {
        	dto.setType(ChatMessageDtoType.IMG);
        	ChatMessageImageDto image = new ChatMessageImageDto();
        	image.setType(message.getType());
        	image.setUuid(message.getImageUuid());
        	// TODO: Get real width and height! For now we only use original image size 640x640px
        	image.setHeight(640);
        	image.setWidth(640);
        	dto.setImage(image);
        }
		dto.setText(message.getText());
		dto.setDisplayName(UserUtils.getDisplayName(message.getUser()));
		dto.setTimeAgo(prettyTime.format(message.getCreatedDate().toDate()));
		
		return dto;
	}

}
