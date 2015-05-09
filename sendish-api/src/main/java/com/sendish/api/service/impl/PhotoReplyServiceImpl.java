package com.sendish.api.service.impl;

import java.awt.Dimension;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sendish.api.dto.PhotoReplyFileUpload;
import com.sendish.api.store.FileStore;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.PhotoReplyRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.PhotoReply;

@Service
@Transactional
public class PhotoReplyServiceImpl {

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

	public PhotoReply processNew(PhotoReplyFileUpload photoReplyFileUpload) {
		MultipartFile file = photoReplyFileUpload.getImage();
		PhotoReply photoReply = mapToPhotoReply(photoReplyFileUpload, file);

		Dimension dimension;
		try {
			dimension = ImageUtils.getDimension(file.getInputStream());
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Cannot read image dimensions for file " + file.getName());
		}
		photoReply.setWidth((int) dimension.getWidth());
		photoReply.setHeight((int) dimension.getHeight());

		String fileStoreId;
		try {
			fileStoreId = fileStore.save(file.getInputStream(),
					"reply_photo_original");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		photoReply.setStorageId(fileStoreId);

		photoReply = photoReplyRepository.save(photoReply);
		chatService.createChatForPhotoReply(photoReply);
		// TODO: Counter for photoReplies on photo

		return photoReply;
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

	public PhotoReply findByUserIdAndPhotoId(Long userId, Long photoId) {
		return photoReplyRepository.findByUserIdAndPhotoId(userId, photoId);
	}

}
