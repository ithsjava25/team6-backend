package org.example.team6backend.activity.service;

import org.example.team6backend.activity.entity.ActivityLog;
import org.example.team6backend.activity.repository.ActivityLogRepository;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

	private final ActivityLogRepository activityLogRepository;

	public ActivityLogService(ActivityLogRepository activityLogRepository) {
		this.activityLogRepository = activityLogRepository;
	}

	public ActivityLog log(String action, String description, Incident incident, AppUser user) {
		ActivityLog activityLog = new ActivityLog();
		activityLog.setAction(action);
		activityLog.setDescription(description);
		activityLog.setIncident(incident);
		activityLog.setUser(user);

		return activityLogRepository.save(activityLog);
	}

	public List<ActivityLog> getByIncidentId(Long incidentId) {
		return activityLogRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);
	}
}
