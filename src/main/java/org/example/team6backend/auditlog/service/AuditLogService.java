package org.example.team6backend.auditlog.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.auditlog.entity.AuditLog;
import org.example.team6backend.auditlog.repository.AuditLogRepository;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
	private final AuditLogRepository auditLogRepository;
	private final AppUserRepository appUserRepository;

	@Transactional
	public void log(String action, String details, AppUser performedBy) {
		log(action, details, performedBy, null, null);
	}

	public void log(String action, String details, AppUser performedBy, String targetType, String targetId) {
		try {
			AuditLog log = new AuditLog();
			log.setUserName(performedBy.getUsername());
			log.setAction(action);
			log.setTargetType(targetType);
			log.setTargetId(targetId);
			log.setDetails(details);
			log.setCreatedAt(Instant.now());
			log.setPerformedBy(performedBy);

			auditLogRepository.save(log);
		} catch (Exception e) {
			log.warn("Failed to write audit log: {}", e.getMessage());
		}
	}
}
