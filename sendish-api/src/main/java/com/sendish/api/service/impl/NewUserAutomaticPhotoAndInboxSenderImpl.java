package com.sendish.api.service.impl;

import com.sendish.repository.AutoSendingInboxMessageRepository;
import com.sendish.repository.AutoSendingPhotoRepository;
import com.sendish.repository.model.jpa.AutoSendingInboxMessage;
import com.sendish.repository.model.jpa.AutoSendingPhoto;
import com.sendish.repository.model.jpa.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class NewUserAutomaticPhotoAndInboxSenderImpl {

    @Autowired
    private AutoSendingPhotoRepository autoSendingPhotoRepository;

    @Autowired
    private AutoSendingInboxMessageRepository autoSendingInboxMessageRepository;

    @Autowired
    private PhotoServiceImpl photoService;

    @Autowired
    private UserInboxServiceImpl userInboxService;

    public void send(User user) {
        sendOnePhoto(user);
        sendInboxMessages(user);
    }

    private void sendOnePhoto(User user) {
        List<AutoSendingPhoto> possiblePhotos = autoSendingPhotoRepository.findAllActiveDefault();

        long count = possiblePhotos.size();
        if (count == 0) {
            return;
        }

        long randomIndex = new Random().nextInt((int) count);

        Optional<AutoSendingPhoto> randomPhoto = StreamSupport.stream(possiblePhotos.spliterator(), false).skip(randomIndex).findFirst();
        photoService.sendPhotoToUser(randomPhoto.get().getPhoto().getId(), user.getId());
    }

    private void sendInboxMessages(User user) {
        List<AutoSendingInboxMessage> inboxMessages = autoSendingInboxMessageRepository.findAllActiveDefault(new Sort(Sort.Direction.ASC, "priority"));

        for (AutoSendingInboxMessage inboxMessage : inboxMessages) {
            userInboxService.sendInboxMessage(inboxMessage.getInboxMessage().getId(), user.getId());
        }
    }

}
