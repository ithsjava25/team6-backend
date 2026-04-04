package org.example.team6backend.user.mapper;

import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(AppUser user) {
		return new UserResponse(user.getId(), user.getGithubId(), user.getGithubLogin(), user.getEmail(),
				user.getName(), user.getRole(), user.getAvatarUrl(), user.isActive(), user.getCreatedAt(),
				user.getUpdatedAt());
	}

	public Page<UserResponse> toResponsePage(Page<AppUser> users) {
		return users.map(this::toResponse);
	}
}
