package org.example.team6backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<AppUser> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream()
                .map(UserResponse::fromEntity)
                .toList());
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserResponse>> getPendingUsers() {
        List<AppUser> users = userService.getUsersByRole(UserRole.PENDING);
        return ResponseEntity.ok(users.stream()
                .map(UserResponse::fromEntity)
                .toList());
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String userId,
            @RequestParam UserRole role) {
        AppUser user = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}