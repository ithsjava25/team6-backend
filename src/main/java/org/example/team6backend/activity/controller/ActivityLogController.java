package org.example.team6backend.activity.controller;

import org.example.team6backend.activity.dto.ActivityLogResponse;
import org.example.team6backend.activity.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityLogController {

	private final ActivityLogService activityLogService;

	public ActivityLogController(ActivityLogService activityLogService) {
		this.activityLogService = activityLogService;
	}

	@GetMapping("/incident/{incidentId}")
	public ResponseEntity<List<ActivityLogResponse>> getActivityByIncidentId(@PathVariable Long incidentId) {
		List<ActivityLogResponse> activityLogs = activityLogService.getByIncidentId(incidentId).stream()
				.map(ActivityLogResponse::fromEntity).toList();

		return ResponseEntity.ok(activityLogs);
	}
}
