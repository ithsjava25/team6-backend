package org.example.team6backend.user.controller;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.mapper.UserMapper;
import org.example.team6backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class,
		OAuth2ClientWebSecurityAutoConfiguration.class})
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserMapper userMapper;

	private AppUser testUser;
	private AppUser adminUser;
	private AppUser handlerUser;
	private AppUser residentUser;

	@BeforeEach
	void setUp() {
		testUser = createUser("user-1", "testuser", "Test User", "test@test.com", UserRole.RESIDENT);
		adminUser = createUser("admin-1", "admin", "Admin User", "admin@test.com", UserRole.ADMIN);
		handlerUser = createUser("handler-1", "handler", "Handler User", "handler@test.com", UserRole.HANDLER);
		residentUser = createUser("resident-1", "resident", "Resident User", "resident@test.com", UserRole.RESIDENT);
	}

	private AppUser createUser(String id, String githubLogin, String name, String email, UserRole role) {
		AppUser user = new AppUser();
		user.setId(id);
		user.setGithubId(id + "123");
		user.setGithubLogin(githubLogin);
		user.setName(name);
		user.setEmail(email);
		user.setRole(role);
		user.setActive(true);
		user.setCreatedAt(Instant.now());
		user.setUpdatedAt(Instant.now());
		return user;
	}

	@Test
	void shouldGetUserById() throws Exception {
		String userId = "user-123";

		when(userService.getUserById(userId)).thenReturn(testUser);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(get("/api/users/{userId}", userId)).andExpect(status().isOk());
	}

	@Test
	void shouldGetUsersByRole_Admin() throws Exception {
		List<AppUser> admins = Arrays.asList(adminUser);

		when(userService.getUsersByRole(UserRole.ADMIN)).thenReturn(admins);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(get("/api/users/role/ADMIN")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	void shouldGetUsersByRole_Handler() throws Exception {
		List<AppUser> handlers = Arrays.asList(handlerUser);

		when(userService.getUsersByRole(UserRole.HANDLER)).thenReturn(handlers);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(get("/api/users/role/HANDLER")).andExpect(status().isOk());
	}

	@Test
	void shouldGetUsersByRole_Resident() throws Exception {
		List<AppUser> residents = Arrays.asList(residentUser);

		when(userService.getUsersByRole(UserRole.RESIDENT)).thenReturn(residents);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(get("/api/users/role/RESIDENT")).andExpect(status().isOk());
	}

	@Test
	void shouldGetUsersByRole_Pending() throws Exception {
		when(userService.getUsersByRole(UserRole.PENDING)).thenReturn(Arrays.asList());

		mockMvc.perform(get("/api/users/role/PENDING")).andExpect(status().isOk()).andExpect(content().json("[]"));
	}

	@Test
	void getUsersByRole_shouldReturnBadRequest_whenRoleIsInvalid() throws Exception {
		mockMvc.perform(get("/api/users/role/INVALID_ROLE")).andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid role: INVALID_ROLE"));
	}

	@Test
	void getUsersByRole_shouldReturnEmptyList_whenServiceReturnsEmpty() throws Exception {
		when(userService.getUsersByRole(UserRole.HANDLER)).thenReturn(Arrays.asList());

		mockMvc.perform(get("/api/users/role/HANDLER")).andExpect(status().isOk()).andExpect(content().json("[]"));
	}
}
