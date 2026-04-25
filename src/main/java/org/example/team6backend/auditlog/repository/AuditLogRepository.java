package org.example.team6backend.auditlog.repository;

import org.example.team6backend.auditlog.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	@Query("SELECT a FROM AuditLog a " + "WHERE LOWER(a.performedBy.name) LIKE LOWER(CONCAT('%', :s, '%')) "
			+ "OR LOWER(a.action) LIKE LOWER(CONCAT('%', :s, '%'))")
	Page<AuditLog> searchLogs(@Param("s") String search, Pageable pageable);

	Page<AuditLog> findByPerformedByIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
