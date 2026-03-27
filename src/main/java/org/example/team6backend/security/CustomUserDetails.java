package org.example.team6backend.security;

import org.example.team6backend.user.entity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Wrappar AppUser för OAuth2-autentisering.
 * Kombinerar GitHub-användardata med vår AppUser-entity.
 */
public class CustomUserDetails implements OAuth2User {

    private final AppUser user;
    private final Map<String, Object> attributes;

    public CustomUserDetails(AppUser user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public String getName() {
        return user.getName() != null ? user.getName() : user.getEmail();
    }

    public AppUser getUser() {
        return user;
    }
}