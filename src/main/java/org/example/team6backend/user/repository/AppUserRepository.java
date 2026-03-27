package org.example.team6backend.user.repository;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByGithubLogin(String githubLogin);

    List<AppUser> findByRole(UserRole role);

    boolean existsByEmail(String email);

    Page<AppUser> findAll(Pageable pageable);

    Page<AppUser> findByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM AppUser u WHERE " +
            "(:email IS NULL OR LOWER(CAST(u.email AS string)) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%'))) AND " +
            "(:name IS NULL OR LOWER(CAST(u.name AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:active IS NULL OR u.active = :active)")
    Page<AppUser> findAllWithFilters(@Param("email") String email,
                                     @Param("name") String name,
                                     @Param("role") UserRole role,
                                     @Param("active") Boolean active,
                                     Pageable pageable);

    @Query("SELECT u FROM AppUser u WHERE " +
            "(:search IS NULL OR " +
            "LOWER(CAST(u.email AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
            "LOWER(CAST(u.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
            "LOWER(CAST(u.githubLogin AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<AppUser> searchUsers(@Param("search") String search, Pageable pageable);
}