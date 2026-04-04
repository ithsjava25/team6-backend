package org.example.team6backend.admin.dto;

import jakarta.validation.constraints.NotNull;
import org.example.team6backend.user.entity.UserRole;

public record UpdateUserRoleRequest(@NotNull(message = "Role is required") UserRole role) {
}
