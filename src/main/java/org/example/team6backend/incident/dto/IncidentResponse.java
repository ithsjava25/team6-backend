package org.example.team6backend.incident.dto;

import lombok.Data;
import org.example.team6backend.document.dto.DocumentDTO;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentCategory;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class IncidentResponse {

	private Long id;
	private String subject;
	private String description;
	private IncidentStatus incidentStatus;
	private IncidentCategory incidentCategory;
	private String createdBy;
	private String assignedTo;
	private LocalDateTime createdAt;
	private boolean hasDocuments;
	private List<DocumentDTO> documents;

	public static IncidentResponse fromEntityBasic(Incident incident) {
		IncidentResponse response = new IncidentResponse();

		response.setId(incident.getId());
		response.setSubject(incident.getSubject());
		response.setDescription(incident.getDescription());
		response.setIncidentStatus(incident.getIncidentStatus());
		response.setIncidentCategory(incident.getIncidentCategory());
		response.setCreatedAt(incident.getCreatedAt());
		response.setHasDocuments(false);
		response.setDocuments(new ArrayList<>());
		response.setCreatedBy(incident.getCreatedBy() != null ? incident.getCreatedBy().getEmail() : null);
		response.setAssignedTo(incident.getAssignedTo() != null ? incident.getAssignedTo().getEmail() : null);

		return response;
	}

	public static IncidentResponse fromEntityWithDocuments(Incident incident) {
		IncidentResponse response = fromEntityBasic(incident);

		try {
			if (Hibernate.isInitialized(incident.getDocuments()) && incident.getDocuments() != null) {
				response.setHasDocuments(!incident.getDocuments().isEmpty());
				if (!incident.getDocuments().isEmpty()) {
					List<DocumentDTO> documentDTOs = incident.getDocuments().stream()
							.filter(document -> document != null).map(document -> {
								DocumentDTO dto = new DocumentDTO();
								dto.setFileName(document.getFileName());
								dto.setFileKey(document.getFileKey());
								return dto;
							}).toList();
					response.setDocuments(documentDTOs);
				}
			}
		} catch (Exception e) {
			response.setHasDocuments(false);
			response.setDocuments(new ArrayList<>());
		}

		return response;
	}

	public static IncidentResponse fromEntity(Incident incident) {
		return fromEntityWithDocuments(incident);
	}
}
