package org.example.team6backend.user.dto;

import org.example.team6backend.user.entity.UserRole;

import java.time.Instant;

public record UserResponse(String id, String githubId, String githubLogin, String email, String name, UserRole role,
		String avatarUrl, boolean active, Instant createdAt, Instant updatedAt) {
}
