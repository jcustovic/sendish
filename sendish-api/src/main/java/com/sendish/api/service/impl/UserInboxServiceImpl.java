package com.sendish.api.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.InboxItemDto;
import com.sendish.api.notification.AsyncNotificationProvider;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.repository.InboxMessageRepository;
import com.sendish.repository.UserInboxItemRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.InboxMessage;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserInboxItem;

@Service
@Transactional
public class UserInboxServiceImpl {

    private static final int INBOX_PAGE_SIZE = 20;

    @Autowired
    private UserInboxItemRepository userInboxItemRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;
    
    @Autowired
    private RedisStatisticsRepository statisticsRepository;
    
    @Autowired
    private AsyncNotificationProvider notificationProvider;

    private static PrettyTime prettyTime = new PrettyTime();

    public List<InboxItemDto> findAll(Long userId, Integer page) {
        List<UserInboxItem> inboxItems = userInboxItemRepository.findByUserId(userId, new PageRequest(page, INBOX_PAGE_SIZE, Sort.Direction.DESC, "createdDate"));

        return mapToInboxItemDto(inboxItems);
    }

    public UserInboxItem findOne(Long itemId, Long userId) {
        return userInboxItemRepository.findByIdAndUserId(itemId, userId);
    }

    public InboxItemDto getByItemIdAndMarkRead(Long itemId, Long userId) {
        UserInboxItem inboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        if (inboxItem == null) {
            return null;
        }

        return getAndMarkAsRead(inboxItem);
    }

    public InboxItemDto getByInboxMessageIdAndMarkRead(Long inboxMessageId, Long userId) {
        UserInboxItem inboxItem = userInboxItemRepository.findByInboxMessageIdAndUserId(inboxMessageId, userId);

        if (inboxItem == null) {
            return null;
        }

        return getAndMarkAsRead(inboxItem);
    }

    public UserInboxItem findByInboxMessageId(Long inboxMessageId, Long userId) {
        return userInboxItemRepository.findByInboxMessageIdAndUserId(inboxMessageId, userId);
    }
    
    public UserInboxItem sendInboxMessage(Long inboxMessageId, Long userId) {
    	User user = userRepository.findOne(userId);
    	InboxMessage inboxMsg = inboxMessageRepository.findOne(inboxMessageId);
    	UserInboxItem userInboxItem = new UserInboxItem();
    	userInboxItem.setInboxMessage(inboxMsg);
    	userInboxItem.setUser(user);
    	
    	userInboxItem = userInboxItemRepository.save(userInboxItem);

    	// TODO: Move after transaction for save is done!
    	statisticsRepository.incrementUnreadInboxItemCount(userId);
    	sendNewInboxItemNotification(userInboxItem);
    	
    	return userInboxItem;
    }

    public void delete(Long itemId, Long userId) {
        UserInboxItem userInboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        userInboxItem.setDeleted(true);
        userInboxItemRepository.save(userInboxItem);
    }

    public void markRead(Long itemId, Long userId) {
        UserInboxItem userInboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        if (!userInboxItem.getRead()) {
        	statisticsRepository.decrementUnreadInboxItemCount(userInboxItem.getUser().getId());
        }
        userInboxItem.setRead(true);
        userInboxItemRepository.save(userInboxItem);
    }

    public void markUnread(Long itemId, Long userId) {
        UserInboxItem userInboxItem = userInboxItemRepository.findByIdAndUserId(itemId, userId);
        if (userInboxItem.getRead()) {
        	statisticsRepository.incrementUnreadInboxItemCount(userInboxItem.getUser().getId());
        }
        userInboxItem.setRead(false);
        userInboxItemRepository.save(userInboxItem);
    }

    private InboxItemDto getAndMarkAsRead(UserInboxItem userInboxItem) {
        if (userInboxItem.getFirstOpenedDate() == null) {
            userInboxItem.setFirstOpenedDate(DateTime.now());
        }
        if (!userInboxItem.getRead()) {
        	statisticsRepository.decrementUnreadInboxItemCount(userInboxItem.getUser().getId());
        }
        userInboxItem.setRead(true);
        
        userInboxItem = userInboxItemRepository.save(userInboxItem);

        return mapToInboxItemDto(userInboxItem);
    }
    
    private void sendNewInboxItemNotification(UserInboxItem userInboxItem) {
		Map<String, Object> customFields = new HashMap<>();
        customFields.put("TYPE", "NEW_INBOX_ITEM");
        customFields.put("REFERENCE_ID", userInboxItem.getId());
        notificationProvider.sendPlainTextNotification("New inbox message", customFields, userInboxItem.getUser().getId());
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
