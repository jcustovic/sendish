package com.sendish.api.security.web.authentication;

import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class UserConnectionSignUp implements ConnectionSignUp {

    private static final Logger LOG = LoggerFactory.getLogger(UserConnectionSignUp.class);

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public final String execute(final Connection<?> p_connection) {
        final UserProfile userProfile = p_connection.fetchUserProfile();

        final String username = getUsername(userProfile, p_connection.getKey().toString());
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username);
        if (user == null) {
            final String randomPassword = UUID.randomUUID().toString();
            User newUser = userService.createUser(username, userProfile.getEmail(), randomPassword, null, false);

            if (StringUtils.hasText(userProfile.getEmail())) {
                newUser.setEmailConfirmed(true);
            }
            if (StringUtils.hasText(userProfile.getFirstName())) {
            	newUser.setFirstName(userProfile.getFirstName());
            	String nickname = org.apache.commons.lang3.StringUtils.left(userProfile.getFirstName(), 20);
                newUser.setNickname(nickname);
            }
            newUser.setLastName(userProfile.getLastName());

            LOG.info("Creating user {} with connectionKey {}", username, p_connection.getKey().toString());
            user = userRepository.save(newUser);
        } else {
            // If email was not confirmed we should now confirm it
            if (StringUtils.hasText(userProfile.getEmail()) && !user.getEmailConfirmed()) {
                user.setEmailConfirmed(true);
                user.setEmail(userProfile.getEmail());
                user = userRepository.save(user);
            } else {
                LOG.warn("Already have existing user with username {} and email {} not accessing with {}", username, userProfile.getEmail(), p_connection.getKey().getProviderId());
            }
        }

        return String.valueOf(user.getId());
    }

    private String getUsername(final UserProfile p_userProfile, final String p_uniqueIdentifier) {
        if (StringUtils.hasText(p_userProfile.getEmail())) {
            return p_userProfile.getEmail();
        }

        if (StringUtils.hasText(p_userProfile.getUsername())) {
            return p_userProfile.getUsername();
        }

        return p_uniqueIdentifier;
    }

}
