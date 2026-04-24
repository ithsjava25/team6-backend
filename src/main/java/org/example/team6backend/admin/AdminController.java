package org.example.team6backend.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.admin.dto.UpdateUserRoleRequest;
import org.example.team6backend.admin.dto.UpdateUserStatusRequest;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.mapper.UserMapper;
import org.example.team6backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

	private final UserService userService;
	private final UserMapper userMapper;

	private AppUser getCurrentUser(CustomUserDetails currentUser) {
		if (currentUser != null && currentUser.getUser() != null) {
			return currentUser.getUser();
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
			return customUserDetails.getUser();
		}
		throw new AccessDeniedException("NNot authenticated!");
	}

	@GetMapping("/users")
	public ResponseEntity<Page<UserResponse>> getUsers(@RequestParam(required = false) String email,
			@RequestParam(required = false) String name, @RequestParam(required = false) UserRole role,
			@RequestParam(required = false) Boolean active, @RequestParam(required = false) String search,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/admin/users - Page: {}, Size: {}, Role: {}, Active: {}, Search: {}",
				pageable.getPageNumber(), pageable.getPageSize(), role, active, search);

		Page<AppUser> users = resolveUsers(email, name, role, active, search, pageable);
		log.debug("Returning {} users out of {}", users.getNumberOfElements(), users.getTotalElements());

		return ResponseEntity.ok(userMapper.toResponsePage(users));
	}

	@GetMapping("/users/pending")
	public ResponseEntity<Page<UserResponse>> getPendingUsers(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/admin/users/pending - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
		var pendingUsers = userService.getUsersByRolePaginated(UserRole.PENDING, pageable);
		log.debug("Found {} pending users", pendingUsers.getTotalElements());

		return ResponseEntity.ok(userMapper.toResponsePage(pendingUsers));
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
		log.info("GET /api/admin/users/{} - Admin fetching user", userId);
		var user = userService.getUserById(userId);
		log.debug("User found: {} ({})", user.getGithubLogin(), user.getRole());

		return ResponseEntity.ok(userMapper.toResponse(user));
	}

	@PostMapping("/users/{userId}/approve")
	public ResponseEntity<UserResponse> approveUser(@PathVariable String userId,
			@AuthenticationPrincipal CustomUserDetails currentUser) {
		AppUser admin = getCurrentUser(currentUser);

		log.info("POST /api/admin/users/{}/approve - Approving pending user", userId);

		AppUser approvedUser = userService.approvePendingUser(userId, admin);
		log.info("User {} approved successfully. New role: {}", approvedUser.getGithubLogin(), approvedUser.getRole());

		return ResponseEntity.ok(userMapper.toResponse(approvedUser));
	}

	@PatchMapping("/users/{userId}/role")
	public ResponseEntity<UserResponse> updateUserRole(@PathVariable String userId,
			@Valid @RequestBody UpdateUserRoleRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {

		AppUser admin = getCurrentUser(currentUser);

		log.info("PATCH /api/admin/users/{}/role - Admin {} changing role to {}", userId, admin.getGithubLogin(),
				request.role());

		if (admin.getId().equals(userId)) { // ← FÖRENKLAT kollen
			log.warn("User {} attempted to change their own role", userId);
			throw new IllegalStateException("You cannot change your own role");
		}

		if (request.role() != UserRole.ADMIN) {
			AppUser targetUser = userService.getUserById(userId);
			if (targetUser.getRole() == UserRole.ADMIN) {
				long adminCount = userService.getAllUsers().stream().filter(u -> u.getRole() == UserRole.ADMIN).count();
				if (adminCount <= 1) {
					log.warn("Attempt to remove last admin user {} by {}", userId, admin.getGithubLogin());
					throw new IllegalStateException("Cannot remove the last admin user");
				}
			}
		}

		AppUser updatedUser = userService.updateUserRole(userId, request.role(), admin); // ← SKICKA MED ADMIN
		log.info("User {} role changed to {} by admin {}", updatedUser.getGithubLogin(), request.role(),
				admin.getGithubLogin());

		return ResponseEntity.ok(userMapper.toResponse(updatedUser));
	}

	@PatchMapping("/users/{userId}/status")
	public ResponseEntity<UserResponse> updateUserStatus(@PathVariable String userId,
			@Valid @RequestBody UpdateUserStatusRequest request,
			@AuthenticationPrincipal CustomUserDetails currentUser) {

		AppUser admin = getCurrentUser(currentUser);

		log.info("PATCH /api/admin/users/{}/status - Admin {} setting active={}", userId, admin.getGithubLogin(),
				request.active());

		if (admin.getId().equals(userId) && !request.active()) {
			log.warn("User {} attempted to deactivate their own account", userId);
			throw new IllegalStateException("You cannot deactivate your own account");
		}

		AppUser updatedUser = userService.updateUserActiveStatus(userId, request.active(), admin);

		log.info("User {} active status changed to {} by admin {}", updatedUser.getGithubLogin(),
				updatedUser.isActive(), admin.getGithubLogin());

		return ResponseEntity.ok(userMapper.toResponse(updatedUser));
	}

	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable String userId,
			@AuthenticationPrincipal CustomUserDetails currentUser) {

		AppUser admin = getCurrentUser(currentUser);

		log.info("DELETE /api/admin/users/{} - Admin {} attempting to delete user", userId, admin.getGithubLogin());

		if (admin.getId().equals(userId)) { // ← FÖRENKLAT
			log.warn("User {} attempted to delete their own account", userId);
			throw new IllegalStateException("You cannot delete your own account");
		}

		var userToDelete = userService.getUserById(userId);
		userService.deleteUser(userId, admin); // ← SKICKA MED ADMIN
		log.info("User {} ({}) deleted by admin {}", userToDelete.getGithubLogin(), userToDelete.getRole(),
				admin.getGithubLogin());

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Long>> getStats() {
		log.info("GET /api/admin/stats - Fetching system statistics");
		Map<String, Long> stats = Map.of("totalUsers", (long) userService.getAllUsers().size(), "pendingUsers",
				(long) userService.getUsersByRole(UserRole.PENDING).size(), "residents",
				(long) userService.getUsersByRole(UserRole.RESIDENT).size(), "handlers",
				(long) userService.getUsersByRole(UserRole.HANDLER).size(), "admins",
				(long) userService.getUsersByRole(UserRole.ADMIN).size());
		log.debug("Stats: Total={}, Pending={}, Residents={}, Handlers={}, Admins={}", stats.get("totalUsers"),
				stats.get("pendingUsers"), stats.get("residents"), stats.get("handlers"), stats.get("admins"));

		return ResponseEntity.ok(stats);
	}

	private Page<AppUser> resolveUsers(String email, String name, UserRole role, Boolean active, String search,
			Pageable pageable) {
		if (search != null && !search.trim().isEmpty()) {
			log.debug("Using search filter: {}", search);
			return userService.searchUsers(search, pageable);
		}

		if (email != null || name != null || role != null || active != null) {
			log.debug("Using filters - email: {}, name: {}, role: {}, active: {}", email, name, role, active);
			return userService.getUsersWithFilters(email, name, role, active, pageable);
		}

		return userService.getAllUsersPaginated(pageable);
	}
}
