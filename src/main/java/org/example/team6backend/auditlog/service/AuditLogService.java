package org.example.team6backend.auditlog.service;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.auditlog.entity.AuditLog;
import org.example.team6backend.auditlog.repository.AuditLogRepository;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService {
	private final AuditLogRepository auditLogRepository;

	public void log(String action, String details, AppUser user) {
		AuditLog log = new AuditLog();
		log.setAction(action);
		log.setDetails(details);
		log.setCreatedAt(Instant.now());
		log.setPerformedBy(user);

		auditLogRepository.save(log);
	}
}
