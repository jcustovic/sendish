package com.sendish.repository;

import com.sendish.repository.model.jpa.UserDetails;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;

public interface UserDetailsRepositoryCustom {

    Page<UserDetails> searchUsersForSendingPool(DateTime lastUserPhotoReceivedDate, int size);

}
