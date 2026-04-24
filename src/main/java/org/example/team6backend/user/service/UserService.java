package org.example.team6backend.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.exception.UserNotFoundException;
import org.example.team6backend.user.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

	private final AppUserRepository userRepository;
	private final EntityManager entityManager;
	private final AuditLogService auditLogService;

	@Transactional
	public AppUser createOrUpdateUser(Map<String, Object> attributes) {
		log.debug("Starting createOrUpdateUser with attributes: {}", attributes.keySet());

		OAuthUserInfo oauthUserInfo = extractOAuthUserInfo(attributes);

		log.info("Processing GitHub login - githubId: {}, githubLogin: {}, email: {}", oauthUserInfo.githubId(),
				oauthUserInfo.githubLogin(), oauthUserInfo.email());

		AppUser user = userRepository.findByGithubId(oauthUserInfo.githubId()).map(existingUser -> {
			log.debug("Existing user found with id: {}", existingUser.getId());
			return updateExistingUser(existingUser, oauthUserInfo);
		}).orElseGet(() -> {
			log.debug("No existing user found, creating new pending user");
			return createNewPendingUser(oauthUserInfo);
		});

		log.info("User processed successfully - userId: {}, githubLogin: {}, role: {}, active: {}", user.getId(),
				user.getGithubLogin(), user.getRole(), user.isActive());

		return user;
	}

	public AppUser getUserById(String id) {
		log.debug("Fetching user by ID: {}", id);
		return userRepository.findById(id).orElseThrow(() -> {
			log.warn("User not found with ID: {}", id);
			return new UserNotFoundException(id);
		});
	}

	public List<AppUser> getAllUsers() {
		log.debug("Fetching all users");
		List<AppUser> users = userRepository.findAll();
		log.info("Retrieved {} users total", users.size());
		return users;
	}

	public List<AppUser> getUsersByRole(UserRole role) {
		log.debug("Fetching users by role: {}", role);
		List<AppUser> users = userRepository.findByRole(role);
		log.info("Retrieved {} users with role: {}", users.size(), role);
		return users;
	}

	public Page<AppUser> getAllUsersPaginated(Pageable pageable) {
		log.debug("Fetching all users paginated - page: {}, size: {}", pageable.getPageNumber(),
				pageable.getPageSize());
		Page<AppUser> page = userRepository.findAll(pageable);
		log.info("Retrieved page {} of {} users (total: {})", pageable.getPageNumber(), page.getNumberOfElements(),
				page.getTotalElements());
		return page;
	}

	public Page<AppUser> getUsersByRolePaginated(UserRole role, Pageable pageable) {
		log.debug("Fetching users by role {} - page: {}, size: {}", role, pageable.getPageNumber(),
				pageable.getPageSize());
		Page<AppUser> page = userRepository.findByRole(role, pageable);
		log.info("Retrieved {} users with role {} (page {})", page.getNumberOfElements(), role,
				pageable.getPageNumber());
		return page;
	}

	public Page<AppUser> getUsersWithFilters(String email, String name, UserRole role, Boolean active,
			Pageable pageable) {
		log.debug("Fetching users with filters - email: {}, name: {}, role: {}, active: {}, page: {}, size: {}", email,
				name, role, active, pageable.getPageNumber(), pageable.getPageSize());
		Page<AppUser> page = userRepository.findAllWithFilters(email, name, role, active, pageable);
		log.info("Retrieved {} users with applied filters (total: {})", page.getNumberOfElements(),
				page.getTotalElements());
		return page;
	}

	public Page<AppUser> searchUsers(String search, Pageable pageable) {
		log.debug("Searching users with term: '{}' - page: {}, size: {}", search, pageable.getPageNumber(),
				pageable.getPageSize());

		if (search == null || search.trim().isEmpty()) {
			log.debug("Empty search term, returning all users paginated");
			Page<AppUser> page = userRepository.findAll(pageable);
			log.info("Retrieved {} users (no search term)", page.getTotalElements());
			return page;
		}

		Page<AppUser> page = userRepository.searchUsers(search.trim(), pageable);
		log.info("Search for '{}' returned {} results (total: {})", search, page.getNumberOfElements(),
				page.getTotalElements());
		return page;
	}

	@Transactional
	public AppUser updateUserRole(String userId, UserRole newRole, AppUser currentAdmin) {
		log.info("Attempting to update role for user: {} to {}", userId, newRole);

		AppUser user = getUserById(userId);
		UserRole oldRole = user.getRole();

		log.debug("User found: current role is {}", oldRole);

		user.setRole(newRole);
		AppUser savedUser = userRepository.save(user);

		auditLogService.log("UPDATE_USER_ROLE",
				currentAdmin.getName() + " changed " + user.getName() + "'s role from " + oldRole + " to " + newRole,
				currentAdmin, "User", userId);

		log.info("Role updated for userId={} from {} to {}", userId, oldRole, newRole);
		return savedUser;
	}

	@Transactional
	public AppUser updateUserActiveStatus(String userId, boolean active, AppUser currentAdmin) {
		log.info("Attempting to update active status for user: {} to active={}", userId, active);

		AppUser user = getUserById(userId);
		boolean oldStatus = user.isActive();

		log.debug("User found: current active status is {}", oldStatus);

		if (!active && user.getRole() == UserRole.ADMIN) {
			long activeAdminCount = userRepository.countByRoleAndActiveTrue(UserRole.ADMIN);
			log.debug("Active admin count: {}", activeAdminCount);

			if (activeAdminCount <= 1) {
				log.warn("Attempt to deactivate last active admin user: {}", userId);
				throw new IllegalStateException("Cannot deactivate the last active admin user");
			}
		}

		user.setActive(active);
		AppUser savedUser = userRepository.save(user);

		auditLogService.log("UPDATE_USER_STATUS",
				currentAdmin.getName() + (active ? " activated " : " deactivated ") + user.getName(), currentAdmin);

		log.info("Active status updated for userId={} from {} to {}", userId, oldStatus, active);
		return savedUser;
	}

	@Transactional
	public AppUser approvePendingUser(String userId, AppUser currentAdmin) {
		log.info("Attempting to approve pending user: {}", userId);

		AppUser user = getUserById(userId);

		if (user.getRole() != UserRole.PENDING) {
			log.warn("User {} is not pending approval. Current role: {}", userId, user.getRole());
			throw new IllegalStateException("User is not pending approval. Current role: " + user.getRole());
		}

		log.debug("User found with role: {}, active: {}", user.getRole(), user.isActive());

		user.setRole(UserRole.RESIDENT);
		user.setActive(true);

		AppUser savedUser = userRepository.save(user);

		auditLogService.log("APPROVE_USER", currentAdmin.getName() + " approved " + user.getName() + " as RESIDENT",
				currentAdmin);

		log.info("User approved successfully: userId={}, githubLogin={}, new role={}", userId, user.getGithubLogin(),
				savedUser.getRole());
		return savedUser;
	}

	@Transactional
	public void deleteUser(String userId, AppUser currentAdmin) {
		log.info("🗑️ Starting deletion process for user: {}", userId);

		AppUser user = getUserById(userId);
		log.debug("User to delete: githubLogin={}, role={}, active={}", user.getGithubLogin(), user.getRole(),
				user.isActive());

		if (user.getRole() == UserRole.ADMIN) {
			long adminCount = userRepository.countByRole(UserRole.ADMIN);
			log.debug("Current admin count: {}", adminCount);

			if (adminCount <= 1) {
				log.warn("🚨 Cannot delete the last admin user: {}", userId);
				throw new IllegalStateException("Cannot delete the last admin user");
			}
		}

		int assignedUpdated = executeUpdate("UPDATE incident SET assigned_to_id = NULL WHERE assigned_to_id = :userId",
				userId, "assigned incidents");

		int createdUpdated = executeUpdate("UPDATE incident SET created_by_id = NULL WHERE created_by_id = :userId",
				userId, "created incidents");

		int modifiedUpdated = executeUpdate("UPDATE incident SET modified_by_id = NULL WHERE modified_by_id = :userId",
				userId, "modified incidents");

		int commentsUpdated = executeUpdate("UPDATE comment SET user_id = NULL WHERE user_id = :userId", userId,
				"comments");

		int activityDeleted = executeUpdate("DELETE FROM activity_log WHERE user_id = :userId", userId,
				"activity log entries");

		int notificationsDeleted = executeUpdate("DELETE FROM notification WHERE user_id = :userId", userId,
				"notifications");

		userRepository.delete(user);

		auditLogService.log("DELETE_USER", currentAdmin.getName() + " deleted user '" + user.getName() + "'",
				currentAdmin, "User", userId);

		log.info("User DELETED successfully: userId={}, githubLogin={}, role={}", userId, user.getGithubLogin(),
				user.getRole());
	}

	private int executeUpdate(String sql, String userId, String entityName) {
		int updated = entityManager.createNativeQuery(sql).setParameter("userId", userId).executeUpdate();
		if (updated > 0) {
			log.debug("Updated/Deleted {} {}", updated, entityName);
		}
		return updated;
	}

	private AppUser updateExistingUser(AppUser existingUser, OAuthUserInfo oauthUserInfo) {
		log.debug("Updating existing user: id={}, old githubLogin={}, new githubLogin={}", existingUser.getId(),
				existingUser.getGithubLogin(), oauthUserInfo.githubLogin());

		existingUser.setGithubLogin(oauthUserInfo.githubLogin());
		existingUser.setEmail(oauthUserInfo.email());
		existingUser.setName(oauthUserInfo.name());
		existingUser.setAvatarUrl(oauthUserInfo.avatarUrl());

		AppUser savedUser = userRepository.save(existingUser);

		log.info("User updated: userId={}, githubId={}, role={}, active={}", savedUser.getId(), savedUser.getGithubId(),
				savedUser.getRole(), savedUser.isActive());

		return savedUser;
	}

	private AppUser createNewPendingUser(OAuthUserInfo oauthUserInfo) {
		log.debug("Creating new pending user: githubLogin={}, email={}", oauthUserInfo.githubLogin(),
				oauthUserInfo.email());

		AppUser newUser = new AppUser();
		newUser.setGithubId(oauthUserInfo.githubId());
		newUser.setGithubLogin(oauthUserInfo.githubLogin());
		newUser.setEmail(oauthUserInfo.email());
		newUser.setName(oauthUserInfo.name());
		newUser.setAvatarUrl(oauthUserInfo.avatarUrl());
		newUser.setRole(UserRole.PENDING);
		newUser.setActive(true);

		AppUser savedUser = userRepository.save(newUser);

		log.info("New pending user created: userId={}, githubId={}, githubLogin={}, role={}", savedUser.getId(),
				savedUser.getGithubId(), savedUser.getGithubLogin(), savedUser.getRole());

		return savedUser;
	}

	private OAuthUserInfo extractOAuthUserInfo(Map<String, Object> attributes) {
		log.debug("Extracting OAuth user info from attributes");

		String githubId = extractRequiredAttribute(attributes, "id");
		String githubLogin = extractRequiredAttribute(attributes, "login");
		String email = extractOptionalAttribute(attributes, "email");
		String name = resolveDisplayName(attributes, githubLogin);
		String avatarUrl = extractOptionalAttribute(attributes, "avatar_url");

		log.debug("Extracted: githubId={}, githubLogin={}, email={}, name={}", githubId, githubLogin, email, name);

		return new OAuthUserInfo(githubId, githubLogin, email, name, avatarUrl);
	}

	private String resolveDisplayName(Map<String, Object> attributes, String githubLogin) {
		String name = extractOptionalAttribute(attributes, "name");
		String displayName = (name == null || name.isBlank()) ? githubLogin : name;
		log.debug("Resolved display name: '{}' from name='{}', githubLogin='{}'", displayName, name, githubLogin);
		return displayName;
	}

	private String extractRequiredAttribute(Map<String, Object> attributes, String key) {
		String value = extractOptionalAttribute(attributes, key);

		if (value == null || value.isBlank()) {
			log.error("Missing required OAuth attribute: {}", key);
			throw new IllegalArgumentException("Missing required OAuth attribute: " + key);
		}

		log.debug("Extracted required attribute '{}': '{}'", key, value);
		return value;
	}

	private String extractOptionalAttribute(Map<String, Object> attributes, String key) {
		Object value = attributes.get(key);
		String result = value != null ? String.valueOf(value) : null;
		if (result != null) {
			log.trace("Extracted optional attribute '{}': '{}'", key, result);
		}
		return result;
	}

	private record OAuthUserInfo(String githubId, String githubLogin, String email, String name, String avatarUrl) {
	}
}
