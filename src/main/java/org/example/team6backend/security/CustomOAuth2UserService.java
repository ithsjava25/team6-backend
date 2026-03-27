package org.example.team6backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.service.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Hanterar OAuth2-inloggning från GitHub.
 * Skapar eller uppdaterar AppUser i databasen baserat på GitHub-data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("GitHub user login: {}", attributes.get("login"));
        log.debug("GitHub user attributes: {}", attributes);

        AppUser user = userService.createOrUpdateUser(attributes);

        return new CustomUserDetails(user, attributes);
    }
}