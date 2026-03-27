package org.example.team6backend.user.dto;

import lombok.Data;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String githubLogin;
    private UserRole role;
    private String avatarUrl;
    private boolean active;

    public static UserResponse fromEntity(AppUser user) {

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setGithubLogin(user.getGithubLogin());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setActive(user.isActive());
        return response;
    }
}