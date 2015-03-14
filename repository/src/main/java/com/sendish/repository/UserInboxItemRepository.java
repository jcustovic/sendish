package com.sendish.repository;

import com.sendish.repository.model.jpa.UserInboxItem;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface UserInboxItemRepository extends JpaRepository<UserInboxItem, Long> {

    @Query("SELECT uii FROM UserInboxItem uii WHERE uii.id = ?1 AND uii.user.id = ?2 AND uii.deleted = false")
    UserInboxItem findByIdAndUserId(Long itemId, Long userId);

    @Query("SELECT uii FROM UserInboxItem uii WHERE uii.inboxMessage.id = ?1 AND uii.user.id = ?2 AND uii.deleted = false")
    UserInboxItem findByInboxMessageIdAndUserId(Long inboxMessageId, Long userId);

    @Query("SELECT uii FROM UserInboxItem uii WHERE uii.user.id = ?1 AND uii.deleted = false")
    List<UserInboxItem> findByUserId(Long userId, Pageable pageRequest);

}
