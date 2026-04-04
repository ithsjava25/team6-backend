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

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserService userService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);
		Map<String, Object> attributes = oauth2User.getAttributes();

		log.info("Received OAuth2 user from provider. login={}, id={}", attributes.get("login"), attributes.get("id"));

		AppUser user = userService.createOrUpdateUser(attributes);

		log.info("OAuth2 login completed for userId={}, role={}", user.getId(), user.getRole());

		return new CustomUserDetails(user, attributes);
	}
}
