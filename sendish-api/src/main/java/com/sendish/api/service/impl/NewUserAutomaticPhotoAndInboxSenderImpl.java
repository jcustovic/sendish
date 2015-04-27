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
import java.util.Random;

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
        sendPhotos(user, 10);
        sendInboxMessages(user);
    }

    private void sendPhotos(User user, int sendCount) {
        List<AutoSendingPhoto> possiblePhotos = autoSendingPhotoRepository.findAllActiveDefault();
        if (possiblePhotos.isEmpty()) {
            return;
        }

        while (sendCount-- > 0 && !possiblePhotos.isEmpty()) {
            int count = possiblePhotos.size();
            int randomIndex = new Random().nextInt(count);

            AutoSendingPhoto photo = possiblePhotos.remove(randomIndex);

            photoService.sendPhotoToUser(photo.getPhoto().getId(), user.getId());
        }
    }

    private void sendInboxMessages(User user) {
        List<AutoSendingInboxMessage> inboxMessages = autoSendingInboxMessageRepository.findAllActiveDefault(new Sort(Sort.Direction.ASC, "priority"));

        for (AutoSendingInboxMessage inboxMessage : inboxMessages) {
            userInboxService.sendInboxMessage(inboxMessage.getInboxMessage().getId(), user.getId());
        }
    }

}
