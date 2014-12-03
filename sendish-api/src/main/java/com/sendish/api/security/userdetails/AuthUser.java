package com.sendish.api.security.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthUser extends User {

    private Long userId;

    public AuthUser(Long p_userId, String p_username, String p_password, Collection<? extends GrantedAuthority> p_authorities) {
        super(p_username, p_password, p_authorities);
        userId = p_userId;
    }

    public AuthUser(Long p_userId, String p_username, String p_password, boolean p_enabled, boolean p_accountNonExpired, boolean p_credentialsNonExpired, boolean p_accountNonLocked, Collection<? extends GrantedAuthority> p_authorities) {
        super(p_username, p_password, p_enabled, p_accountNonExpired, p_credentialsNonExpired, p_accountNonLocked, p_authorities);
        userId = p_userId;
    }

    public Long getUserId() {
        return userId;
    }

}
