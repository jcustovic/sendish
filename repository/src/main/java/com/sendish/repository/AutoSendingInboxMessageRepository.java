package com.sendish.repository;

import com.sendish.repository.model.jpa.AutoSendingInboxMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface AutoSendingInboxMessageRepository extends JpaRepository<AutoSendingInboxMessage, Long> {

    @Query(value = "SELECT asim FROM AutoSendingInboxMessage asim WHERE asim.active = true AND asim.city IS NULL AND asim.country IS NULL AND asim.afterDays IS NULL AND asim.onDate IS NULL")
    List<AutoSendingInboxMessage> findAllActiveDefault(Sort sort);

}
