package org.example.team6backend.incident.repository;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

	@EntityGraph(attributePaths = {"documents", "createdBy", "assignedTo"})
	Page<Incident> findAll(Pageable pageable);

	@EntityGraph(attributePaths = {"documents", "createdBy", "assignedTo"})
	Page<Incident> findByCreatedBy(AppUser user, Pageable pageable);

	@EntityGraph(attributePaths = {"documents", "createdBy", "assignedTo"})
	Page<Incident> findByAssignedTo(AppUser user, Pageable pageable);

	@EntityGraph(attributePaths = {"documents", "createdBy", "assignedTo"})
	Page<Incident> findByIncidentStatus(IncidentStatus status, Pageable pageable);

	@Query("SELECT i FROM Incident i LEFT JOIN FETCH i.documents WHERE i.id = :id")
	Optional<Incident> findByIdWithDocuments(@Param("id") Long id);

	@Query("SELECT i FROM Incident i WHERE " + "LOWER(i.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(COALESCE(i.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))")
	Page<Incident> searchIncidents(@Param("search") String search, Pageable pageable);
}
