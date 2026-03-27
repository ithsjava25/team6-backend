package org.example.team6backend.incident.repository;

import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByCreatedBy(AppUser user);
    List <Incident> findByAssignedTo(AppUser user);
}
