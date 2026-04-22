package org.example.team6backend.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.mapper.UserMapper;
import org.example.team6backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		log.info("GET /api/users/me - User: {}", userDetails.getUser().getGithubLogin());
		return ResponseEntity.ok(userMapper.toResponse(userDetails.getUser()));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
		log.info("GET /api/users/{} - Fetching user by ID", userId);
		var user = userService.getUserById(userId);
		log.debug("User found: {} ({})", user.getGithubLogin(), user.getRole());
		return ResponseEntity.ok(userMapper.toResponse(user));
	}

	@GetMapping("/role/{role}")
	public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
		log.info("GET /api/users/role/{} - Fetching users by role", role);
		try {
			var userRole = org.example.team6backend.user.entity.UserRole.valueOf(role.toUpperCase());
			var users = userService.getUsersByRole(userRole);
			log.debug("Found {} users with role {}", users.size(), role);
			return ResponseEntity.ok(users.stream().map(userMapper::toResponse).toList());
		} catch (IllegalArgumentException e) {
			log.warn("Invalid role requested: {}", role);
			return ResponseEntity.badRequest().body("Invalid role: " + role);
		}
	}
}
