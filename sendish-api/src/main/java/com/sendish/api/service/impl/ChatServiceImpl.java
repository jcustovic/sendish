package com.sendish.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.ChatThreadRepository;
import com.sendish.repository.ChatThreadUserRepository;
import com.sendish.repository.model.jpa.ChatThread;
import com.sendish.repository.model.jpa.ChatThreadUser;
import com.sendish.repository.model.jpa.PhotoReply;

@Service
@Transactional
public class ChatServiceImpl {
	
	@Autowired
	private ChatThreadRepository chatThreadRepository;
	
	@Autowired
	private ChatThreadUserRepository chatThreadUserRepository;

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

}
