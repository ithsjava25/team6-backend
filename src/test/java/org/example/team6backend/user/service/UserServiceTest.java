package org.example.team6backend.user.service;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private AppUser createTestUser() {
        AppUser user = new AppUser();
        user.setId("123e4567-e89b-12d3-a456-426614174000");
        user.setGithubId("12345678");
        user.setGithubLogin("testuser");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRole(UserRole.RESIDENT);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}