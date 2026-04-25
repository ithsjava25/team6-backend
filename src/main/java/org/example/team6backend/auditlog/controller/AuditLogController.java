package org.example.team6backend.auditlog.controller;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.auditlog.entity.AuditLog;
import org.example.team6backend.auditlog.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

	private final AuditLogRepository auditLogRepository;

	@GetMapping
	public ResponseEntity<Page<AuditLog>> getLogs(@RequestParam(required = false) String search,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<AuditLog> logs;
		if (search != null && !search.isEmpty()) {
			logs = auditLogRepository.searchLogs(search, pageable);
		} else {
			logs = auditLogRepository.findAll(pageable);
		}
		return ResponseEntity.ok(logs);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Page<AuditLog>> getLogsByUser(@PathVariable String userId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<AuditLog> logs = auditLogRepository.findByPerformedByIdOrderByCreatedAtDesc(userId, pageable);
		return ResponseEntity.ok(logs);
	}
}
