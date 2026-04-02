package org.example.team6backend.user.repository;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByGithubId(String githubId);

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRole(UserRole role);

    long countByRole(UserRole role);

    long countByRoleAndActiveTrue(UserRole role);

    Page<AppUser> findByRole(UserRole role, Pageable pageable);

    Page<AppUser> findByActive(boolean active, Pageable pageable);

    @Query("""
            SELECT u
            FROM AppUser u
            WHERE (:email IS NULL OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:name IS NULL OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:active IS NULL OR u.active = :active)
            """)
    Page<AppUser> findAllWithFilters(
            @Param("email") String email,
            @Param("name") String name,
            @Param("role") UserRole role,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
            SELECT u
            FROM AppUser u
            WHERE LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(u.githubLogin, '')) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(u.githubId, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<AppUser> searchUsers(@Param("search") String search, Pageable pageable);
}