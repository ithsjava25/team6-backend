package org.example.team6backend.activity.repository;

import org.example.team6backend.activity.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
	List<ActivityLog> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
