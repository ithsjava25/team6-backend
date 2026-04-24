package org.example.team6backend.auditlog.repository;

import org.example.team6backend.auditlog.entity.AuditLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	@Query("SELECT a FROM AuditLog a " + "WHERE LOWER(a.userName) LIKE LOWER(CONCAT('%', :s, '%')) "
			+ "OR LOWER(a.action) LIKE LOWER(CONCAT('%', :s, '%'))")

	List<AuditLog> searchLogs(@Param("s") String search, Sort sort);

	List<AuditLog> findByPerformedBy(Long userId);
}
