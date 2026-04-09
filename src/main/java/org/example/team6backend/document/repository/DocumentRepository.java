package org.example.team6backend.document.repository;

import org.example.team6backend.document.entity.Document;
import org.example.team6backend.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	List<Document> findByIncident(Incident incident);
}
