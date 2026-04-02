package org.example.team6backend.user.dto;

import org.example.team6backend.user.entity.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String githubId,
        String githubLogin,
        String email,
        String name,
        UserRole role,
        String avatarUrl,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}