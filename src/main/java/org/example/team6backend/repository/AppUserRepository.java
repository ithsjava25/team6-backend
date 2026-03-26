package org.example.team6backend.repository;

import org.example.team6backend.entity.AppUser;
import org.example.team6backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByGithubLogin(String githubLogin);

    List<AppUser> findByRole(UserRole role);

    boolean existsByEmail(String email);
}