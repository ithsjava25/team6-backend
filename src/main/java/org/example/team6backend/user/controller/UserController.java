package org.example.team6backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userMapper.toResponse(userDetails.getUser());
    }
}