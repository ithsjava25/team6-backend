package org.example.team6backend.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	@Transactional
	public AppUser createOrUpdateUser(Map<String, Object> attributes) {
		OAuthUserInfo oauthUserInfo = extractOAuthUserInfo(attributes);

		log.info("Processing GitHub login. githubId={}, githubLogin={}", oauthUserInfo.githubId(),
				oauthUserInfo.githubLogin());

		return userRepository.findByGithubId(oauthUserInfo.githubId())
				.map(existingUser -> updateExistingUser(existingUser, oauthUserInfo))
				.orElseGet(() -> createNewPendingUser(oauthUserInfo));
	}

	public AppUser getUserById(String id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	public List<AppUser> getAllUsers() {
		return userRepository.findAll();
	}

	public List<AppUser> getUsersByRole(UserRole role) {
		return userRepository.findByRole(role);
	}

	public Page<AppUser> getAllUsersPaginated(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	public Page<AppUser> getUsersByRolePaginated(UserRole role, Pageable pageable) {
		return userRepository.findByRole(role, pageable);
	}

	public Page<AppUser> getUsersWithFilters(String email, String name, UserRole role, Boolean active,
			Pageable pageable) {
		return userRepository.findAllWithFilters(email, name, role, active, pageable);
	}

	public Page<AppUser> searchUsers(String search, Pageable pageable) {
		if (search == null || search.trim().isEmpty()) {
			return userRepository.findAll(pageable);
		}

		return userRepository.searchUsers(search.trim(), pageable);
	}

	@Transactional
	public AppUser updateUserRole(String userId, UserRole newRole) {
		AppUser user = getUserById(userId);
		UserRole oldRole = user.getRole();

		user.setRole(newRole);
		AppUser savedUser = userRepository.save(user);

		log.info("Updated role for userId={} from {} to {}", userId, oldRole, newRole);
		return savedUser;
	}

	@Transactional
	public AppUser updateUserActiveStatus(String userId, boolean active) {
		AppUser user = getUserById(userId);

		if (!active && user.getRole() == UserRole.ADMIN) {
			long activeAdminCount = userRepository.countByRoleAndActiveTrue(UserRole.ADMIN);
			if (activeAdminCount <= 1) {
				throw new IllegalStateException("Cannot deactivate the last active admin user");
			}
		}

		boolean oldStatus = user.isActive();
		user.setActive(active);
		AppUser savedUser = userRepository.save(user);

		log.info("Updated active status for userId={} from {} to {}", userId, oldStatus, active);
		return savedUser;
	}

	@Transactional
	public AppUser approvePendingUser(String userId) {
		AppUser user = getUserById(userId);

		if (user.getRole() != UserRole.PENDING) {
			throw new IllegalStateException("User is not pending approval. Current role: " + user.getRole());
		}

		user.setRole(UserRole.RESIDENT);
		user.setActive(true);

		AppUser savedUser = userRepository.save(user);
		log.info("Approved pending user: userId={}, githubLogin={}", userId, user.getGithubLogin());
		return savedUser;
	}

	@Transactional
	public void deleteUser(String userId) {
		AppUser user = getUserById(userId);

		if (user.getRole() == UserRole.ADMIN) {
			long adminCount = userRepository.countByRole(UserRole.ADMIN);
			if (adminCount <= 1) {
				throw new IllegalStateException("Cannot delete the last admin user");
			}
		}

		int assignedUpdated = entityManager
				.createNativeQuery("UPDATE incident SET assigned_to_id = NULL WHERE assigned_to_id = :userId")
				.setParameter("userId", userId).executeUpdate();
		log.debug("Updated {} incidents where user was assigned", assignedUpdated);

		int createdUpdated = entityManager
				.createNativeQuery("UPDATE incident SET created_by_id = NULL WHERE created_by_id = :userId")
				.setParameter("userId", userId).executeUpdate();
		log.debug("Updated {} incidents where user was creator", createdUpdated);

		int modifiedUpdated = entityManager
				.createNativeQuery("UPDATE incident SET modified_by_id = NULL WHERE modified_by_id = :userId")
				.setParameter("userId", userId).executeUpdate();
		log.debug("Updated {} incidents where user was modifier", modifiedUpdated);

		int commentsUpdated = entityManager
				.createNativeQuery("UPDATE comment SET user_id = NULL WHERE user_id = :userId")
				.setParameter("userId", userId).executeUpdate();
		log.debug("Updated {} comments", commentsUpdated);

		int activityDeleted = entityManager.createNativeQuery("DELETE FROM activity_log WHERE user_id = :userId")
				.setParameter("userId", userId).executeUpdate();
		log.debug("Deleted {} activity log entries", activityDeleted);

		int notificationsDeleted = entityManager.createNativeQuery("DELETE FROM notification WHERE user_id = :userId")
				.setParameter("userId", userId).executeUpdate();
		log.debug("Deleted {} notifications", notificationsDeleted);

		userRepository.delete(user);
		log.info("Deleted user: userId={}, githubLogin={}, role={}", userId, user.getGithubLogin(), user.getRole());
	}

	private AppUser updateExistingUser(AppUser existingUser, OAuthUserInfo oauthUserInfo) {
		existingUser.setGithubLogin(oauthUserInfo.githubLogin());
		existingUser.setEmail(oauthUserInfo.email());
		existingUser.setName(oauthUserInfo.name());
		existingUser.setAvatarUrl(oauthUserInfo.avatarUrl());

		AppUser savedUser = userRepository.save(existingUser);

		log.info("Updated existing user. userId={}, githubId={}, role={}, active={}", savedUser.getId(),
				savedUser.getGithubId(), savedUser.getRole(), savedUser.isActive());

		return savedUser;
	}

	private AppUser createNewPendingUser(OAuthUserInfo oauthUserInfo) {
		AppUser newUser = new AppUser();
		newUser.setGithubId(oauthUserInfo.githubId());
		newUser.setGithubLogin(oauthUserInfo.githubLogin());
		newUser.setEmail(oauthUserInfo.email());
		newUser.setName(oauthUserInfo.name());
		newUser.setAvatarUrl(oauthUserInfo.avatarUrl());
		newUser.setRole(UserRole.PENDING);
		newUser.setActive(true);

		AppUser savedUser = userRepository.save(newUser);

		log.info("Created new user. userId={}, githubId={}, role={}", savedUser.getId(), savedUser.getGithubId(),
				savedUser.getRole());

		return savedUser;
	}

	private OAuthUserInfo extractOAuthUserInfo(Map<String, Object> attributes) {
		String githubId = extractRequiredAttribute(attributes, "id");
		String githubLogin = extractRequiredAttribute(attributes, "login");
		String email = extractOptionalAttribute(attributes, "email");
		String name = resolveDisplayName(attributes, githubLogin);
		String avatarUrl = extractOptionalAttribute(attributes, "avatar_url");

		return new OAuthUserInfo(githubId, githubLogin, email, name, avatarUrl);
	}

	private String resolveDisplayName(Map<String, Object> attributes, String githubLogin) {
		String name = extractOptionalAttribute(attributes, "name");
		return (name == null || name.isBlank()) ? githubLogin : name;
	}

	private String extractRequiredAttribute(Map<String, Object> attributes, String key) {
		String value = extractOptionalAttribute(attributes, key);

		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Missing required OAuth attribute: " + key);
		}

		return value;
	}

	private String extractOptionalAttribute(Map<String, Object> attributes, String key) {
		Object value = attributes.get(key);
		return value != null ? String.valueOf(value) : null;
	}

	private record OAuthUserInfo(String githubId, String githubLogin, String email, String name, String avatarUrl) {
	}
}
