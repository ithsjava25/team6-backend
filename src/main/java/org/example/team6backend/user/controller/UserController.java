package org.example.team6backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/api/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        AppUser user = userDetails.getUser();
        return UserResponse.fromEntity(user);
    }
}