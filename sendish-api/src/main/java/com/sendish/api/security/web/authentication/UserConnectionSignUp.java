package com.sendish.api.security.web.authentication;

import java.util.UUID;

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

import com.sendish.api.util.EntitySynchronizer;

public class UserConnectionSignUp implements ConnectionSignUp {

    private static final Logger          LOG = LoggerFactory.getLogger(UserConnectionSignUp.class);

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntitySynchronizer entitySynchronizer;

    @Override
    public final String execute(final Connection<?> p_connection) {
        final UserProfile userProfile = p_connection.fetchUserProfile();

        final String username = getUsername(userProfile, p_connection.getKey().toString());
        entitySynchronizer.lock(username);
        try {
            User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username);
            if (user == null) {
                final String randomPassword = UUID.randomUUID().toString();
                User newUser = userService.createUser(username, userProfile.getEmail(), randomPassword, null, false);

                newUser.setFirstName(userProfile.getFirstName());
                newUser.setLastName(userProfile.getLastName());
                if (StringUtils.hasText(userProfile.getEmail())) {
                    newUser.setEmailConfirmed(true);
                }

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
        } finally {
            entitySynchronizer.unlock();
        }
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
