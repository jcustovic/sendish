package com.sendish.api.service.impl;

import com.sendish.api.dto.admin.CreateInboxMessage;
import com.sendish.api.dto.admin.InboxMessageDto;
import com.sendish.repository.InboxMessageRepository;
import com.sendish.repository.model.jpa.*;
import com.sendish.repository.model.jpa.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InboxMessageServiceImpl {

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private ImageServiceImpl imageService;

    private static final int INBOX_MSG_PAGE_SIZE = 20;

    public List<InboxMessageDto> findAll(Integer page) {
        Page<InboxMessage> inboxMessages = inboxMessageRepository.findAll(new PageRequest(page, INBOX_MSG_PAGE_SIZE, Sort.Direction.DESC, "createdDate"));

        return inboxMessages.getContent().stream().map(im -> mapToInboxMessageDto(im)).collect(Collectors.toList());
    }

    private InboxMessageDto mapToInboxMessageDto(InboxMessage inboxMessage) {
        InboxMessageDto dto = new InboxMessageDto();
        dto.setId(inboxMessage.getId());
        dto.setCreatedDate(inboxMessage.getCreatedDate());
        dto.setImageSize(inboxMessage.getImage().getSize());
        dto.setImageUuid(inboxMessage.getImage().getUuid());
        dto.setMessage(inboxMessage.getMessage());
        dto.setShortTitle(inboxMessage.getShortTitle());
        dto.setTitle(inboxMessage.getTitle());
        dto.setUrl(inboxMessage.getUrl());

        return dto;
    }

    public InboxMessageDto findOne(Long inboxMessageId) {
        InboxMessage inboxMessage = inboxMessageRepository.findOne(inboxMessageId);
        if (inboxMessage == null) {
            return null;
        } else {
            return mapToInboxMessageDto(inboxMessage);
        }
    }

    public InboxMessageDto createNew(CreateInboxMessage createInboxMessage) {
        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setMessage(createInboxMessage.getMessage());
        inboxMessage.setShortTitle(createInboxMessage.getShortTitle());
        inboxMessage.setTitle(createInboxMessage.getTitle());
        inboxMessage.setUrl(createInboxMessage.getUrl());

        Image image = imageService.create(createInboxMessage.getImage());
        inboxMessage.setImage(image);

        inboxMessage = inboxMessageRepository.save(inboxMessage);

        return mapToInboxMessageDto(inboxMessage);
    }

}
