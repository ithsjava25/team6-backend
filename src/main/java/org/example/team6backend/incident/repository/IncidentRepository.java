package org.example.team6backend.incident.repository;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

	Page<Incident> findByCreatedBy(AppUser user, Pageable pageable);
	Page<Incident> findByAssignedTo(AppUser user, Pageable pageable);
}
