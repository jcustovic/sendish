package com.sendish.api.security.authentication;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.repository.model.jpa.User;

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private transient UserServiceImpl userService;

    @Override
    public final UserDetails loadUserByUsername(final String p_username) {
        final User user = userService.findByUsernameIgnoreCaseOrEmailIgnoreCase(p_username, p_username);
        if (user == null) {
            throw new UsernameNotFoundException("Username " + p_username + " not found");
        }

        return convertToUser(user);
    }

    public UserDetails loadUserById(Long userId) {
        final User user = userService.findOne(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User with id " + userId + " not found");
        }

        return convertToUser(user);
    }

    private UserDetails convertToUser(User user) {
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("USER"));

        boolean disabled = user.getDisabled() || (user.getEmailRegistration() && !user.getEmailConfirmed());
        final AuthUser userDetails = new AuthUser(user.getId(), user.getUsername(), user.getPassword(), disabled, true, true, !user.getDeleted(), roles);

        return userDetails;
    }

}
