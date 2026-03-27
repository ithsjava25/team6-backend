package org.example.team6backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.entity.AppUser;
import org.example.team6backend.entity.UserRole;
import org.example.team6backend.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
            log.info("Updated existing user: {} (role: {})", email, user.getRole());
            return userRepository.save(user);
        } else {
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setGithubLogin(githubLogin);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setRole(UserRole.PENDING);
            log.info("Created new user: {} with role PENDING (awaiting approval)", email);
            return userRepository.save(newUser);
        }
    }

    public AppUser getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    public List<AppUser> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public AppUser updateUserRole(String userId, UserRole newRole) {
        AppUser user = getUserById(userId);
        user.setRole(newRole);
        log.info("Updated role for user {} to {}", userId, newRole);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<AppUser> getAllUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AppUser> getUsersByRolePaginated(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AppUser> getUsersWithFilters(String email, String name, UserRole role, Boolean active, Pageable pageable) {
        return userRepository.findAllWithFilters(email, name, role, active, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AppUser> searchUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchUsers(search.trim(), pageable);
    }
}