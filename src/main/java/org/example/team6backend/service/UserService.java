package org.example.team6backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.entity.AppUser;
import org.example.team6backend.entity.UserRole;
import org.example.team6backend.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppUserRepository userRepository;

    @Transactional
    public AppUser createOrUpdateUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String githubLogin = (String) attributes.get("login");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("avatar_url");

        log.info("GitHub user data - login: {}, email: {}, name: {}", githubLogin, email, name);

        if (email == null || email.isEmpty()) {
            email = githubLogin + "@users.noreply.github.com";
            log.warn("Email was null for user {}, using fallback: {}", githubLogin, email);
        }

        Optional<AppUser> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            user.setName(name);
            user.setGithubLogin(githubLogin);
            user.setAvatarUrl(avatarUrl);
            log.info("Updated existing user: {}", email);
            return userRepository.save(user);
        } else {
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setGithubLogin(githubLogin);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setRole(UserRole.RESIDENT);
            log.info("Created new user: {} with role RESIDENT", email);
            return userRepository.save(newUser);
        }
    }
}