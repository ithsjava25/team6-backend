package org.example.team6backend.security;

import org.example.team6backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private CustomOAuth2UserService customOAuth2UserService;

	@Test
	void serviceShouldBeCreated() {
		assertThat(customOAuth2UserService).isNotNull();
	}

	@Test
	void userServiceShouldBeInjected() {
		assertThat(userService).isNotNull();
	}
}
