package org.example.team6backend.admin;

import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.security.CustomUserDetails;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class,
		OAuth2ClientWebSecurityAutoConfiguration.class})
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserMapper userMapper;

	@MockitoBean
	private AuditLogService auditLogService;

	private AppUser testUser;
	private AppUser pendingUser;
	private AppUser handlerUser;
	private AppUser adminUser;
	private UsernamePasswordAuthenticationToken auth;

	@BeforeEach
	void setUp() {
		testUser = new AppUser();
		testUser.setId(UUID.randomUUID().toString());
		testUser.setGithubId("test123");
		testUser.setGithubLogin("testuser");
		testUser.setName("Test User");
		testUser.setEmail("test@test.com");
		testUser.setRole(UserRole.RESIDENT);
		testUser.setActive(true);
		testUser.setCreatedAt(Instant.now());
		testUser.setUpdatedAt(Instant.now());

		pendingUser = new AppUser();
		pendingUser.setId(UUID.randomUUID().toString());
		pendingUser.setGithubId("pending123");
		pendingUser.setGithubLogin("pending");
		pendingUser.setName("Pending User");
		pendingUser.setEmail("pending@test.com");
		pendingUser.setRole(UserRole.PENDING);
		pendingUser.setActive(true);
		pendingUser.setCreatedAt(Instant.now());
		pendingUser.setUpdatedAt(Instant.now());

		handlerUser = new AppUser();
		handlerUser.setId(UUID.randomUUID().toString());
		handlerUser.setGithubId("handler123");
		handlerUser.setGithubLogin("handler");
		handlerUser.setName("Handler User");
		handlerUser.setEmail("handler@test.com");
		handlerUser.setRole(UserRole.HANDLER);
		handlerUser.setActive(true);
		handlerUser.setCreatedAt(Instant.now());
		handlerUser.setUpdatedAt(Instant.now());

		adminUser = new AppUser();
		adminUser.setId(UUID.randomUUID().toString());
		adminUser.setGithubId("admin123");
		adminUser.setGithubLogin("admin");
		adminUser.setName("Admin User");
		adminUser.setEmail("admin@test.com");
		adminUser.setRole(UserRole.ADMIN);
		adminUser.setActive(true);
		adminUser.setCreatedAt(Instant.now());
		adminUser.setUpdatedAt(Instant.now());

		CustomUserDetails principal = new CustomUserDetails(adminUser, Map.of());
		auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldGetAllUsers() throws Exception {
		Pageable pageable = PageRequest.of(0, 20);
		List<AppUser> users = Arrays.asList(testUser, handlerUser, pendingUser);
		Page<AppUser> userPage = new PageImpl<>(users, pageable, users.size());

		when(userService.getAllUsersPaginated(any(Pageable.class))).thenReturn(userPage);
		when(userMapper.toResponsePage(any(Page.class))).thenReturn(new PageImpl<>(Arrays.asList()));

		mockMvc.perform(get("/api/admin/users").with(authentication(auth))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldGetUsersWithSearch() throws Exception {
		String searchTerm = "test";
		Pageable pageable = PageRequest.of(0, 20);
		List<AppUser> users = Arrays.asList(testUser);
		Page<AppUser> userPage = new PageImpl<>(users, pageable, users.size());

		when(userService.searchUsers(eq(searchTerm), any(Pageable.class))).thenReturn(userPage);
		when(userMapper.toResponsePage(any(Page.class))).thenReturn(new PageImpl<>(Arrays.asList()));

		mockMvc.perform(get("/api/admin/users").param("search", searchTerm).with(authentication(auth)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldGetPendingUsers() throws Exception {
		Pageable pageable = PageRequest.of(0, 20);
		List<AppUser> pendingUsers = Arrays.asList(pendingUser);
		Page<AppUser> pendingPage = new PageImpl<>(pendingUsers, pageable, pendingUsers.size());

		when(userService.getUsersByRolePaginated(eq(UserRole.PENDING), any(Pageable.class))).thenReturn(pendingPage);
		when(userMapper.toResponsePage(any(Page.class))).thenReturn(new PageImpl<>(Arrays.asList()));

		mockMvc.perform(get("/api/admin/users/pending").with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldGetUserById() throws Exception {
		String userId = testUser.getId();

		when(userService.getUserById(userId)).thenReturn(testUser);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(get("/api/admin/users/{userId}", userId).with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldApprovePendingUser() throws Exception {
		String userId = pendingUser.getId();
		AppUser approvedUser = pendingUser;
		approvedUser.setRole(UserRole.RESIDENT);

		when(userService.approvePendingUser(eq(userId), any(AppUser.class))).thenReturn(approvedUser);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(post("/api/admin/users/{userId}/approve", userId).with(authentication(auth)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldUpdateUserRole() throws Exception {
		String userId = testUser.getId();
		String requestBody = "{\"role\":\"ADMIN\"}";
		AppUser updatedUser = testUser;
		updatedUser.setRole(UserRole.ADMIN);

		when(userService.getUserById(userId)).thenReturn(testUser);
		when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, updatedUser));
		when(userService.updateUserRole(eq(userId), eq(UserRole.ADMIN), any(AppUser.class))).thenReturn(updatedUser);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(patch("/api/admin/users/{userId}/role", userId).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody).with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldUpdateUserStatus() throws Exception {
		String userId = testUser.getId();
		String requestBody = "{\"active\":false}";
		AppUser updatedUser = testUser;
		updatedUser.setActive(false);

		when(userService.updateUserActiveStatus(eq(userId), eq(false), any(AppUser.class))).thenReturn(updatedUser);
		when(userMapper.toResponse(any(AppUser.class))).thenReturn(null);

		mockMvc.perform(patch("/api/admin/users/{userId}/status", userId).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody).with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldDeleteUser() throws Exception {
		String userId = testUser.getId();

		when(userService.getUserById(userId)).thenReturn(testUser);
		doNothing().when(userService).deleteUser(eq(userId), any(AppUser.class));

		mockMvc.perform(delete("/api/admin/users/{userId}", userId).with(authentication(auth)))
				.andExpect(status().isNoContent());

		verify(userService).deleteUser(eq(userId), any(AppUser.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldGetStats() throws Exception {
		List<AppUser> allUsers = Arrays.asList(testUser, pendingUser, handlerUser);
		List<AppUser> pendingUsers = Arrays.asList(pendingUser);
		List<AppUser> residents = Arrays.asList(testUser);
		List<AppUser> handlers = Arrays.asList(handlerUser);
		List<AppUser> admins = Arrays.asList();

		when(userService.getAllUsers()).thenReturn(allUsers);
		when(userService.getUsersByRole(UserRole.PENDING)).thenReturn(pendingUsers);
		when(userService.getUsersByRole(UserRole.RESIDENT)).thenReturn(residents);
		when(userService.getUsersByRole(UserRole.HANDLER)).thenReturn(handlers);
		when(userService.getUsersByRole(UserRole.ADMIN)).thenReturn(admins);

		mockMvc.perform(get("/api/admin/stats").with(authentication(auth))).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalUsers").value(3)).andExpect(jsonPath("$.pendingUsers").value(1))
				.andExpect(jsonPath("$.residents").value(1)).andExpect(jsonPath("$.handlers").value(1))
				.andExpect(jsonPath("$.admins").value(0));
	}
}
