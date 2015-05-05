package com.sendish.repository;

import com.sendish.repository.model.jpa.PushToken;
import com.sendish.repository.model.jpa.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PushTokenRepository<T extends PushToken> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    List<T> findByUser(User p_user);

}
