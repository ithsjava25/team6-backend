package org.example.team6backend.dev;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DevAuthControllerTest {

	@Mock
	private AppUserRepository userRepository;

	@InjectMocks
	private DevAuthController devAuthController;

	private MockMvc mockMvc;
	private AppUser testUser;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(devAuthController).build();

		testUser = new AppUser();
		testUser.setId("123");
		testUser.setGithubLogin("testuser");
		testUser.setGithubId("github123");
		testUser.setName("Test User");
		testUser.setEmail("test@example.com");
		testUser.setRole(UserRole.RESIDENT);
		testUser.setActive(true);
		testUser.setAvatarUrl("https://example.com/avatar.png");
	}

	@Test
	@DisplayName("Should return dev mode enabled = true")
	void isDevModeEnabled_shouldReturnTrue() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);

		mockMvc.perform(get("/dev/enabled")).andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	@DisplayName("Should return dev mode enabled = false")
	void isDevModeEnabled_shouldReturnFalse() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", false);

		mockMvc.perform(get("/dev/enabled")).andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
	}

	@Test
	@DisplayName("Should return test page when dev mode enabled")
	void test_shouldReturnHtmlPage() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);

		mockMvc.perform(get("/dev/test")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Dev endpoint works")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Dev mode enabled: true")));
	}

	@Test
	@DisplayName("Should return all users as JSON when dev mode enabled")
	void getAllUsers_shouldReturnUsersList() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);
		when(userRepository.findAll()).thenReturn(List.of(testUser));

		mockMvc.perform(get("/dev/all-users")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$[0].githubLogin").value("testuser"))
				.andExpect(jsonPath("$[0].name").value("Test User"))
				.andExpect(jsonPath("$[0].email").value("test@example.com"))
				.andExpect(jsonPath("$[0].role").value("RESIDENT")).andExpect(jsonPath("$[0].active").value(true));

		verify(userRepository).findAll();
	}

	@Test
	@DisplayName("Should return 403 when dev mode disabled for all-users")
	void getAllUsers_shouldReturn403WhenDevDisabled() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", false);

		mockMvc.perform(get("/dev/all-users")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Should show inactive user styling in HTML")
	void listUsers_shouldShowInactiveUserStatus() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);

		AppUser inactiveUser = new AppUser();
		inactiveUser.setId("456");
		inactiveUser.setGithubLogin("inactive");
		inactiveUser.setName("Inactive User");
		inactiveUser.setEmail("inactive@example.com");
		inactiveUser.setRole(UserRole.RESIDENT);
		inactiveUser.setActive(false);

		when(userRepository.findAll()).thenReturn(List.of(inactiveUser));

		mockMvc.perform(get("/dev/users")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("inactive-user")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("status-inactive")));
	}

	@Test
	@DisplayName("Should return 403 for /users when dev mode disabled")
	void listUsers_shouldReturn403WhenDevDisabled() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", false);

		mockMvc.perform(get("/dev/users")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Should switch user and redirect to dashboard.html when dev mode enabled")
	void switchUser_shouldSwitchAndRedirect() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);
		when(userRepository.findByGithubLogin("testuser")).thenReturn(Optional.of(testUser));

		mockMvc.perform(get("/dev/switch-user?githubLogin=testuser")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/dashboard.html"));

		verify(userRepository).findByGithubLogin("testuser");
	}

	@Test
	@DisplayName("Should return 403 when dev mode disabled for switch-user")
	void switchUser_shouldReturn403WhenDevDisabled() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", false);

		mockMvc.perform(get("/dev/switch-user?githubLogin=testuser")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Should return error page when user is inactive")
	void switchUser_shouldReturnErrorWhenUserInactive() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);

		AppUser inactiveUser = new AppUser();
		inactiveUser.setGithubLogin("inactive");
		inactiveUser.setName("Inactive User");
		inactiveUser.setActive(false);
		inactiveUser.setRole(UserRole.RESIDENT);

		when(userRepository.findByGithubLogin("inactive")).thenReturn(Optional.of(inactiveUser));

		mockMvc.perform(get("/dev/switch-user?githubLogin=inactive")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Account Inactive")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("cannot switch to an inactive user")));
	}

	@Test
	@DisplayName("Should escape special characters in JSON response")
	void getAllUsers_shouldEscapeSpecialCharactersInJson() throws Exception {
		ReflectionTestUtils.setField(devAuthController, "devModeEnabled", true);

		AppUser specialUser = new AppUser();
		specialUser.setId("789");
		specialUser.setGithubLogin("test\"user");
		specialUser.setName("Test \"User\"");
		specialUser.setEmail("test@example.com");
		specialUser.setRole(UserRole.RESIDENT);
		specialUser.setActive(true);

		when(userRepository.findAll()).thenReturn(List.of(specialUser));

		mockMvc.perform(get("/dev/all-users")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("test\\\"user")));
	}
}
