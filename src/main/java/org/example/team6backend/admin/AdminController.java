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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AppUser> users = resolveUsers(email, name, role, active, search, pageable);
        return ResponseEntity.ok(userMapper.toResponsePage(users));
    }

    @GetMapping("/users/pending")
    public ResponseEntity<Page<UserResponse>> getPendingUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                userMapper.toResponsePage(userService.getUsersByRolePaginated(UserRole.PENDING, pageable))
        );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userMapper.toResponse(userService.getUserById(userId)));
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<UserResponse> approveUser(@PathVariable String userId) {
        AppUser approvedUser = userService.approvePendingUser(userId);
        return ResponseEntity.ok(userMapper.toResponse(approvedUser));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

       if (currentUser.getUser().getId().equals(userId)) {
           throw new IllegalStateException("You cannot change your own role");
       }

        if (request.role() != UserRole.ADMIN) {
            AppUser targetUser = userService.getUserById(userId);
            if (targetUser.getRole() == UserRole.ADMIN) {
                long adminCount = userService.getAllUsers().stream()
                        .filter(u -> u.getRole() == UserRole.ADMIN)
                        .count();
                if (adminCount <= 1) {
                    throw new IllegalStateException("Cannot remove the last admin user");
                }
            }
        }

        AppUser updatedUser = userService.updateUserRole(userId, request.role());
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

       if (currentUser.getUser().getId().equals(userId) && !request.active()) {
           throw new IllegalStateException("You cannot deactivate your own account");
       }

        AppUser updatedUser = userService.updateUserActiveStatus(userId, request.active());
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You cannot delete your own account");
        }

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = Map.of(
                "totalUsers", (long) userService.getAllUsers().size(),
                "pendingUsers", (long) userService.getUsersByRole(UserRole.PENDING).size(),
                "residents", (long) userService.getUsersByRole(UserRole.RESIDENT).size(),
                "handlers", (long) userService.getUsersByRole(UserRole.HANDLER).size(),
                "admins", (long) userService.getUsersByRole(UserRole.ADMIN).size()
        );
        return ResponseEntity.ok(stats);
    }

    private Page<AppUser> resolveUsers(
            String email,
            String name,
            UserRole role,
            Boolean active,
            String search,
            Pageable pageable
    ) {
        if (search != null && !search.trim().isEmpty()) {
            return userService.searchUsers(search, pageable);
        }

        if (email != null || name != null || role != null || active != null) {
            return userService.getUsersWithFilters(email, name, role, active, pageable);
        }

        return userService.getAllUsersPaginated(pageable);
    }
}