package org.example.team6backend.auditlog.controller;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.auditlog.entity.AuditLog;
import org.example.team6backend.auditlog.repository.AuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

	private final AuditLogRepository auditLogRepository;

	@GetMapping
	public ResponseEntity<List<AuditLog>> getLogs(@RequestParam(required = false) String search) {
		List<AuditLog> logs;
		if (search != null && !search.isEmpty()) {
			logs = auditLogRepository.searchLogs(search, Sort.by(Sort.Direction.DESC, "createdAt"));
		} else {
			logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
		}
		return ResponseEntity.ok(logs);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable Long userId) {
		List<AuditLog> logs = auditLogRepository.findByPerformedBy(userId);
		return ResponseEntity.ok(logs);
	}

}
