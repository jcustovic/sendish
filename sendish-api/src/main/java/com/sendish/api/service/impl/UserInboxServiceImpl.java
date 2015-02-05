package com.sendish.api.service.impl;

import com.sendish.api.dto.InboxItemDto;
import com.sendish.repository.UserInboxItemRepository;
import com.sendish.repository.model.jpa.InboxMessage;
import com.sendish.repository.model.jpa.UserInboxItem;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserInboxServiceImpl {

    private static final int INBOX_PAGE_SIZE = 20;

    @Autowired
    private UserInboxItemRepository userInboxItemRepository;

    private static PrettyTime prettyTime = new PrettyTime();

    public List<InboxItemDto> findAll(Long userId, Integer page) {
        List<UserInboxItem> inboxItems = userInboxItemRepository.findByUserId(userId, new PageRequest(page, INBOX_PAGE_SIZE, Sort.Direction.DESC, "createdDate"));

        return mapToInboxItemDto(inboxItems);
    }

    public UserInboxItem findOne(Long itemId, Long userId) {
        return userInboxItemRepository.findByIdAndUserId(itemId, userId);
    }

    public InboxItemDto getOne(Long itemId, Long userId) {
        UserInboxItem inboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        if (inboxItem == null) {
            return null;
        }

        return getAndMarkAsRead(inboxItem);
    }

    public InboxItemDto getOneByInboxMessageId(Long inboxMessageId, Long userId) {
        UserInboxItem inboxItem = userInboxItemRepository.findByInboxMessageIdAndUserId(inboxMessageId, userId);

        if (inboxItem == null) {
            return null;
        }

        return getAndMarkAsRead(inboxItem);
    }

    public void delete(Long itemId, Long userId) {
        UserInboxItem userInboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        userInboxItem.setDeleted(true);
        userInboxItemRepository.save(userInboxItem);
    }

    public void markRead(Long itemId, Long userId) {
        UserInboxItem userInboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        userInboxItem.setRead(true);
        userInboxItemRepository.save(userInboxItem);
    }

    public void markUnread(Long itemId, Long userId) {
        UserInboxItem userInboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        userInboxItem.setRead(false);
        userInboxItemRepository.save(userInboxItem);
    }

    private InboxItemDto getAndMarkAsRead(UserInboxItem inboxItem) {
        if (inboxItem.getFirstOpenedDate() == null) {
            inboxItem.setFirstOpenedDate(DateTime.now());
        }
        inboxItem.setRead(true);
        inboxItem = userInboxItemRepository.save(inboxItem);

        return mapToInboxItemDto(inboxItem);
    }

    private List<InboxItemDto> mapToInboxItemDto(List<UserInboxItem> inboxItems) {
        return inboxItems.stream().map(item -> mapToInboxItemDto(item)).collect(Collectors.toList());
    }

    private InboxItemDto mapToInboxItemDto(UserInboxItem inboxItem) {
        InboxMessage inboxMessage = inboxItem.getInboxMessage();
        InboxItemDto inboxItemDto = new InboxItemDto();
        inboxItemDto.setImageUuid(inboxMessage.getImage().getUuid());
        inboxItemDto.setMessage(inboxMessage.getMessage());
        inboxItemDto.setShortTitle(inboxMessage.getShortTitle());
        inboxItemDto.setTitle(inboxMessage.getTitle());
        inboxItemDto.setUrl(inboxMessage.getUrl());
        inboxItemDto.setId(inboxItem.getId());
        inboxItemDto.setTimeAgo(getPrettyTime(inboxItem.getCreatedDate()));
        inboxItemDto.setRead(inboxItem.getRead());

        return inboxItemDto;
    }

    private String getPrettyTime(DateTime dateTime) {
        return prettyTime.format(dateTime.toDate());
    }

}
