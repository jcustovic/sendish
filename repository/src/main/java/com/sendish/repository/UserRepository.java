package com.sendish.repository;

import com.sendish.repository.model.jpa.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameIgnoreCaseOrEmailIgnoreCase(String p_username, String p_email);

    User findByUsernameIgnoreCase(String username);

}
