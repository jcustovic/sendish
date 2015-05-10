package com.sendish.api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.ChatMessageDto;
import com.sendish.api.dto.ChatMessageDto.ChatMessageDtoType;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.ChatMessageRepository;
import com.sendish.repository.ChatThreadRepository;
import com.sendish.repository.ChatThreadUserRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.ChatMessage;
import com.sendish.repository.model.jpa.ChatThread;
import com.sendish.repository.model.jpa.ChatThreadUser;
import com.sendish.repository.model.jpa.ChatThreadUserId;
import com.sendish.repository.model.jpa.PhotoReply;

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
	
	public ChatMessageDto newChatMessage(Long chatThreadId, Long userId, String text) {
		ChatThread chatThread = chatThreadRepository.findOne(chatThreadId);
		chatThread.setLastActivity(DateTime.now());
		chatThreadRepository.save(chatThread);
		
		ChatMessage message = new ChatMessage();
		message.setChatThread(chatThread);
		message.setUser(userRepository.findOne(userId));
		message.setText(text);
		
		return mapToMessageDto(chatMessageRepository.save(message));
	}
	
	private ChatMessageDto mapToMessageDto(ChatMessage message) {
		ChatMessageDto dto = new ChatMessageDto();
		dto.setId(message.getId());
		dto.setText(message.getText());
		dto.setType(ChatMessageDtoType.TEXT);
		dto.setUsername(UserUtils.getDisplayName(message.getUser()));
		dto.setTime(prettyTime.format(message.getCreatedDate().toDate()));
		
		return dto;
	}

}
