package org.example.team6backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.dto.UserResponse;
import org.example.team6backend.entity.AppUser;
import org.example.team6backend.entity.UserRole;
import org.example.team6backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AppUser> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search, pageable);
        }
        else if (email != null || name != null || role != null || active != null) {
            users = userService.getUsersWithFilters(email, name, role, active, pageable);
        }
        else {
            users = userService.getAllUsersPaginated(pageable);
        }

        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(
            @PathVariable UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AppUser> users = userService.getUsersByRolePaginated(role, pageable);
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/users/pending")
    public ResponseEntity<Page<UserResponse>> getPendingUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AppUser> users = userService.getUsersByRolePaginated(UserRole.PENDING, pageable);
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        AppUser user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String userId,
            @RequestParam UserRole role) {
        AppUser user = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}