package org.example.team6backend.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.team6backend.admin.dto.UpdateUserRoleRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(
            @PathVariable UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(userMapper.toResponsePage(userService.getUsersByRolePaginated(role, pageable)));
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

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        AppUser updatedUser = userService.updateUserRole(userId, request.role());
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
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