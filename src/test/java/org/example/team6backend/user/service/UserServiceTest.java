package org.example.team6backend.user.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.exception.UserNotFoundException;
import org.example.team6backend.user.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private AppUserRepository userRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private Query mockQuery;

	@Mock
	private AuditLogService auditLogService;

	@InjectMocks
	private UserService userService;

	private AppUser testUser;
	private AppUser adminUser;
	private AppUser pendingUser;
	private AppUser handlerUser;
	private AppUser inactiveUser;
	private String testUserId;
	private String adminUserId;
	private String pendingUserId;
	private String handlerUserId;
	private String inactiveUserId;

	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID().toString();
		adminUserId = UUID.randomUUID().toString();
		pendingUserId = UUID.randomUUID().toString();
		handlerUserId = UUID.randomUUID().toString();
		inactiveUserId = UUID.randomUUID().toString();

		testUser = new AppUser();
		testUser.setId(testUserId);
		testUser.setGithubId("test123");
		testUser.setGithubLogin("testuser");
		testUser.setName("Test User");
		testUser.setEmail("test@test.com");
		testUser.setRole(UserRole.RESIDENT);
		testUser.setActive(true);
		testUser.setCreatedAt(Instant.now());
		testUser.setUpdatedAt(Instant.now());

		adminUser = new AppUser();
		adminUser.setId(adminUserId);
		adminUser.setGithubId("admin123");
		adminUser.setGithubLogin("admin");
		adminUser.setName("Admin User");
		adminUser.setEmail("admin@test.com");
		adminUser.setRole(UserRole.ADMIN);
		adminUser.setActive(true);
		adminUser.setCreatedAt(Instant.now());
		adminUser.setUpdatedAt(Instant.now());

		pendingUser = new AppUser();
		pendingUser.setId(pendingUserId);
		pendingUser.setGithubId("pending123");
		pendingUser.setGithubLogin("pending");
		pendingUser.setName("Pending User");
		pendingUser.setEmail("pending@test.com");
		pendingUser.setRole(UserRole.PENDING);
		pendingUser.setActive(true);
		pendingUser.setCreatedAt(Instant.now());
		pendingUser.setUpdatedAt(Instant.now());

		handlerUser = new AppUser();
		handlerUser.setId(handlerUserId);
		handlerUser.setGithubId("handler123");
		handlerUser.setGithubLogin("handler");
		handlerUser.setName("Handler User");
		handlerUser.setEmail("handler@test.com");
		handlerUser.setRole(UserRole.HANDLER);
		handlerUser.setActive(true);
		handlerUser.setCreatedAt(Instant.now());
		handlerUser.setUpdatedAt(Instant.now());

		inactiveUser = new AppUser();
		inactiveUser.setId(inactiveUserId);
		inactiveUser.setGithubId("inactive123");
		inactiveUser.setGithubLogin("inactive");
		inactiveUser.setName("Inactive User");
		inactiveUser.setEmail("inactive@test.com");
		inactiveUser.setRole(UserRole.RESIDENT);
		inactiveUser.setActive(false);
		inactiveUser.setCreatedAt(Instant.now());
		inactiveUser.setUpdatedAt(Instant.now());
	}

	@Test
	void getUserById_ShouldReturnUser_WhenExists() {

		when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

		AppUser found = userService.getUserById(testUserId);

		assertThat(found).isNotNull();
		assertThat(found.getId()).isEqualTo(testUserId);
		assertThat(found.getGithubLogin()).isEqualTo("testuser");
		assertThat(found.getRole()).isEqualTo(UserRole.RESIDENT);
		verify(userRepository).findById(testUserId);
	}

	@Test
	void getUserById_ShouldReturnAdmin_WhenExists() {

		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));

		AppUser found = userService.getUserById(adminUserId);

		assertThat(found).isNotNull();
		assertThat(found.getRole()).isEqualTo(UserRole.ADMIN);
		verify(userRepository).findById(adminUserId);
	}

	@Test
	void getUserById_ShouldThrowException_WhenNotFound() {

		String nonExistentId = "non-existent-id";
		when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUserById(nonExistentId)).isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(nonExistentId);
		verify(userRepository).findById(nonExistentId);
	}

	@Test
	void getAllUsers_ShouldReturnAllUsers() {

		List<AppUser> users = Arrays.asList(testUser, adminUser, pendingUser, handlerUser, inactiveUser);
		when(userRepository.findAll()).thenReturn(users);

		List<AppUser> result = userService.getAllUsers();

		assertThat(result).hasSize(5);
		assertThat(result).containsExactly(testUser, adminUser, pendingUser, handlerUser, inactiveUser);
		verify(userRepository).findAll();
	}

	@Test
	void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {

		when(userRepository.findAll()).thenReturn(Collections.emptyList());

		List<AppUser> result = userService.getAllUsers();

		assertThat(result).isEmpty();
		verify(userRepository).findAll();
	}

	@Test
	void getUsersByRole_ShouldReturnUsersWithSpecificRole() {

		List<AppUser> residents = Arrays.asList(testUser, inactiveUser);
		when(userRepository.findByRole(UserRole.RESIDENT)).thenReturn(residents);

		List<AppUser> result = userService.getUsersByRole(UserRole.RESIDENT);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getRole()).isEqualTo(UserRole.RESIDENT);
		assertThat(result.get(1).getRole()).isEqualTo(UserRole.RESIDENT);
		verify(userRepository).findByRole(UserRole.RESIDENT);
	}

	@Test
	void getUsersByRole_ShouldReturnEmptyList_WhenNoUsersWithRole() {

		when(userRepository.findByRole(UserRole.PENDING)).thenReturn(Collections.emptyList());

		List<AppUser> result = userService.getUsersByRole(UserRole.PENDING);

		assertThat(result).isEmpty();
		verify(userRepository).findByRole(UserRole.PENDING);
	}

	@Test
	void getUsersByRole_ShouldReturnOnlyAdmins() {
		List<AppUser> admins = Arrays.asList(adminUser);

		when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(admins);

		List<AppUser> result = userService.getUsersByRole(UserRole.ADMIN);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getRole()).isEqualTo(UserRole.ADMIN);
		verify(userRepository).findByRole(UserRole.ADMIN);
	}

	@Test
	void getAllUsersPaginated_ShouldReturnPageOfUsers() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> users = Arrays.asList(testUser, adminUser);
		Page<AppUser> userPage = new PageImpl<>(users, pageable, users.size());
		when(userRepository.findAll(pageable)).thenReturn(userPage);

		Page<AppUser> result = userService.getAllUsersPaginated(pageable);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);
		verify(userRepository).findAll(pageable);
	}

	@Test
	void getAllUsersPaginated_ShouldReturnEmptyPage_WhenNoUsers() {

		Pageable pageable = PageRequest.of(0, 10);
		Page<AppUser> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
		when(userRepository.findAll(pageable)).thenReturn(emptyPage);

		Page<AppUser> result = userService.getAllUsersPaginated(pageable);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
		verify(userRepository).findAll(pageable);
	}

	@Test
	void getUsersByRolePaginated_ShouldReturnPageOfUsersByRole() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> admins = Arrays.asList(adminUser);
		Page<AppUser> adminPage = new PageImpl<>(admins, pageable, admins.size());
		when(userRepository.findByRole(UserRole.ADMIN, pageable)).thenReturn(adminPage);

		Page<AppUser> result = userService.getUsersByRolePaginated(UserRole.ADMIN, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.ADMIN);
		verify(userRepository).findByRole(UserRole.ADMIN, pageable);
	}

	@Test
	void getUsersWithFilters_ShouldReturnFilteredUsers() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> filteredUsers = Arrays.asList(testUser);
		Page<AppUser> filteredPage = new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
		when(userRepository.findAllWithFilters(any(), any(), any(), any(), eq(pageable))).thenReturn(filteredPage);

		Page<AppUser> result = userService.getUsersWithFilters("test", null, null, true, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(userRepository).findAllWithFilters("test", null, null, true, pageable);
	}

	@Test
	void getUsersWithFilters_ShouldFilterByMultipleCriteria() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> filteredUsers = Arrays.asList(adminUser);
		Page<AppUser> filteredPage = new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
		when(userRepository.findAllWithFilters(eq("admin"), eq("Admin"), eq(UserRole.ADMIN), eq(true), eq(pageable)))
				.thenReturn(filteredPage);

		Page<AppUser> result = userService.getUsersWithFilters("admin", "Admin", UserRole.ADMIN, true, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.ADMIN);
		verify(userRepository).findAllWithFilters("admin", "Admin", UserRole.ADMIN, true, pageable);
	}

	@Test
	void searchUsers_WithValidSearch_ShouldReturnMatchingUsers() {

		String searchTerm = "test";
		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> matchingUsers = Arrays.asList(testUser);
		Page<AppUser> searchPage = new PageImpl<>(matchingUsers, pageable, matchingUsers.size());
		when(userRepository.searchUsers(searchTerm, pageable)).thenReturn(searchPage);

		Page<AppUser> result = userService.searchUsers(searchTerm, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(userRepository).searchUsers(searchTerm, pageable);
	}

	@Test
	void searchUsers_WithEmptySearch_ShouldReturnAllUsers() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> allUsers = Arrays.asList(testUser, adminUser);
		Page<AppUser> allPage = new PageImpl<>(allUsers, pageable, allUsers.size());
		when(userRepository.findAll(pageable)).thenReturn(allPage);

		Page<AppUser> result = userService.searchUsers("", pageable);

		assertThat(result.getContent()).hasSize(2);
		verify(userRepository).findAll(pageable);
		verify(userRepository, never()).searchUsers(any(), any());
	}

	@Test
	void searchUsers_WithNullSearch_ShouldReturnAllUsers() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> allUsers = Arrays.asList(testUser, adminUser);
		Page<AppUser> allPage = new PageImpl<>(allUsers, pageable, allUsers.size());
		when(userRepository.findAll(pageable)).thenReturn(allPage);

		Page<AppUser> result = userService.searchUsers(null, pageable);

		assertThat(result.getContent()).hasSize(2);
		verify(userRepository).findAll(pageable);
		verify(userRepository, never()).searchUsers(any(), any());
	}

	@Test
	void searchUsers_WithWhitespaceOnly_ShouldReturnAllUsers() {

		Pageable pageable = PageRequest.of(0, 10);
		List<AppUser> allUsers = Arrays.asList(testUser, adminUser);
		Page<AppUser> allPage = new PageImpl<>(allUsers, pageable, allUsers.size());
		when(userRepository.findAll(pageable)).thenReturn(allPage);

		Page<AppUser> result = userService.searchUsers("   ", pageable);

		assertThat(result.getContent()).hasSize(2);
		verify(userRepository).findAll(pageable);
		verify(userRepository, never()).searchUsers(any(), any());
	}

	@Test
	void updateUserRole_ShouldUpdateRole() {

		UserRole newRole = UserRole.ADMIN;
		when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.updateUserRole(testUserId, newRole, adminUser);

		assertThat(updated.getRole()).isEqualTo(newRole);
		verify(userRepository).save(testUser);
		verify(auditLogService).log(anyString(), anyString(), eq(adminUser), anyString(), anyString());
	}

	@Test
	void updateUserRole_ShouldUpdateFromHandlerToAdmin() {

		UserRole newRole = UserRole.ADMIN;
		when(userRepository.findById(handlerUserId)).thenReturn(Optional.of(handlerUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.updateUserRole(handlerUserId, newRole, adminUser);

		assertThat(updated.getRole()).isEqualTo(UserRole.ADMIN);
		verify(userRepository).save(handlerUser);
	}

	@Test
	void updateUserRole_ShouldUpdateFromAdminToHandler() {

		UserRole newRole = UserRole.HANDLER;
		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.updateUserRole(adminUserId, newRole, adminUser);

		assertThat(updated.getRole()).isEqualTo(UserRole.HANDLER);
		verify(userRepository).save(adminUser);
	}

	@Test
	void updateUserActiveStatus_ShouldDeactivateUser() {

		boolean newStatus = false;
		when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.updateUserActiveStatus(testUserId, newStatus, adminUser);

		assertThat(updated.isActive()).isFalse();
		verify(userRepository).save(testUser);
	}

	@Test
	void updateUserActiveStatus_ShouldActivateUser() {

		when(userRepository.findById(inactiveUserId)).thenReturn(Optional.of(inactiveUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.updateUserActiveStatus(inactiveUserId, true, adminUser);

		assertThat(updated.isActive()).isTrue();
		verify(userRepository).save(inactiveUser);
	}

	@Test
	void updateUserActiveStatus_ShouldThrowException_WhenDeactivatingLastAdmin() {

		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByRoleAndActiveTrue(UserRole.ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> userService.updateUserActiveStatus(adminUserId, false, adminUser))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Cannot deactivate the last active admin user");
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserActiveStatus_ShouldAllowDeactivatingAdmin_WhenMultipleAdminsExist() {

		boolean newStatus = false;
		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByRoleAndActiveTrue(UserRole.ADMIN)).thenReturn(2L);
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.updateUserActiveStatus(adminUserId, newStatus, adminUser);

		assertThat(updated.isActive()).isFalse();
		verify(userRepository).save(adminUser);
	}

	@Test
	void approvePendingUser_ShouldApproveAndChangeRoleToResident() {

		when(userRepository.findById(pendingUserId)).thenReturn(Optional.of(pendingUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser approved = userService.approvePendingUser(pendingUserId, adminUser);

		assertThat(approved.getRole()).isEqualTo(UserRole.RESIDENT);
		assertThat(approved.isActive()).isTrue();
		verify(userRepository).save(pendingUser);
	}

	@Test
	void approvePendingUser_ShouldThrowException_WhenUserIsNotPending() {

		when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

		assertThatThrownBy(() -> userService.approvePendingUser(testUserId, adminUser))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("User is not pending approval");
		verify(userRepository, never()).save(any());
	}

	@Test
	void approvePendingUser_ShouldThrowException_WhenUserIsAdmin() {

		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));

		assertThatThrownBy(() -> userService.approvePendingUser(adminUserId, adminUser))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("User is not pending approval");
		verify(userRepository, never()).save(any());
	}

	@Test
	void approvePendingUser_ShouldThrowException_WhenUserIsResident() {

		when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

		assertThatThrownBy(() -> userService.approvePendingUser(testUserId, adminUser))
				.isInstanceOf(IllegalStateException.class);
		verify(userRepository, never()).save(any());
	}

	@Test
	void deleteUser_ShouldDeleteUserAndCleanupRelations() {

		lenient().when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
		lenient().when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(2L);
		lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
		lenient().when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
		lenient().when(mockQuery.executeUpdate()).thenReturn(1);
		lenient().doNothing().when(userRepository).delete(testUser);

		userService.deleteUser(testUserId, adminUser);

		verify(userRepository).delete(testUser);
		verify(entityManager, atLeast(6)).createNativeQuery(anyString());
	}

	@Test
	void deleteUser_ShouldThrowException_WhenDeletingLastAdmin() {

		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> userService.deleteUser(adminUserId, adminUser))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Cannot delete the last admin user");
		verify(userRepository, never()).delete(any());
	}

	@Test
	void deleteUser_ShouldAllowDeletingAdmin_WhenMultipleAdminsExist() {

		when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(2L);
		when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
		when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
		when(mockQuery.executeUpdate()).thenReturn(1);
		doNothing().when(userRepository).delete(adminUser);

		userService.deleteUser(adminUserId, adminUser);

		verify(userRepository).delete(adminUser);
		verify(entityManager, atLeast(6)).createNativeQuery(anyString());
	}

	@Test
	void createOrUpdateUser_ShouldCreateNewPendingUser_WhenUserDoesNotExist() {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", 12345L);
		attributes.put("login", "newuser");
		attributes.put("name", "New User");
		attributes.put("email", "new@test.com");
		attributes.put("avatar_url", "http://avatar.com/pic.jpg");

		when(userRepository.findByGithubId("12345")).thenReturn(Optional.empty());
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser created = userService.createOrUpdateUser(attributes);

		assertThat(created.getRole()).isEqualTo(UserRole.PENDING);
		assertThat(created.isActive()).isTrue();
		assertThat(created.getGithubLogin()).isEqualTo("newuser");
		assertThat(created.getName()).isEqualTo("New User");
		assertThat(created.getEmail()).isEqualTo("new@test.com");
		assertThat(created.getAvatarUrl()).isEqualTo("http://avatar.com/pic.jpg");
		verify(userRepository).save(any(AppUser.class));
	}

	@Test
	void createOrUpdateUser_ShouldUpdateExistingUser_WhenUserExists() {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", 12345L);
		attributes.put("login", "updateduser");
		attributes.put("name", "Updated Name");
		attributes.put("email", "updated@test.com");
		attributes.put("avatar_url", "http://avatar.com/updated.jpg");

		when(userRepository.findByGithubId("12345")).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser updated = userService.createOrUpdateUser(attributes);

		assertThat(updated.getGithubLogin()).isEqualTo("updateduser");
		assertThat(updated.getName()).isEqualTo("Updated Name");
		assertThat(updated.getEmail()).isEqualTo("updated@test.com");
		assertThat(updated.getAvatarUrl()).isEqualTo("http://avatar.com/updated.jpg");
		verify(userRepository).save(testUser);
	}

	@Test
	void createOrUpdateUser_ShouldUseGithubLoginAsName_WhenNameIsMissing() {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", 12345L);
		attributes.put("login", "newuser");
		attributes.put("email", "new@test.com");

		when(userRepository.findByGithubId("12345")).thenReturn(Optional.empty());
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser created = userService.createOrUpdateUser(attributes);

		assertThat(created.getName()).isEqualTo("newuser");
		verify(userRepository).save(any(AppUser.class));
	}

	@Test
	void createOrUpdateUser_ShouldHandleMissingAvatarUrl() {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", 12345L);
		attributes.put("login", "newuser");
		attributes.put("name", "New User");
		attributes.put("email", "new@test.com");

		when(userRepository.findByGithubId("12345")).thenReturn(Optional.empty());
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser created = userService.createOrUpdateUser(attributes);

		assertThat(created.getAvatarUrl()).isNull();
		verify(userRepository).save(any(AppUser.class));
	}

	@Test
	void createOrUpdateUser_ShouldThrowException_WhenGithubIdIsMissing() {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("login", "newuser");

		assertThatThrownBy(() -> userService.createOrUpdateUser(attributes))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Missing required OAuth attribute");
		verify(userRepository, never()).save(any());
	}

	@Test
	void createOrUpdateUser_ShouldThrowException_WhenGithubLoginIsMissing() {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", 12345L);

		assertThatThrownBy(() -> userService.createOrUpdateUser(attributes))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Missing required OAuth attribute");
		verify(userRepository, never()).save(any());
	}
}
