package org.example.team6backend.incident.dto;

import lombok.Data;
import org.example.team6backend.document.dto.DocumentDTO;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentCategory;
import org.example.team6backend.incident.entity.IncidentStatus;

import java.time.LocalDateTime;
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

	public static IncidentResponse fromEntity(Incident incident) {
		IncidentResponse response = new IncidentResponse();

		response.setId(incident.getId());
		response.setSubject(incident.getSubject());
		response.setDescription(incident.getDescription());
		response.setIncidentStatus(incident.getIncidentStatus());
		response.setIncidentCategory(incident.getIncidentCategory());
		response.setCreatedAt(incident.getCreatedAt());
		response.setDocuments(incident.getDocuments().stream().map(document -> {
			DocumentDTO dto = new DocumentDTO();
			dto.setFileName(document.getFileName());
			dto.setFileKey(document.getFileKey());
			return dto;
		}).toList());

		response.setHasDocuments(incident.getDocuments() != null && !incident.getDocuments().isEmpty());

		response.setCreatedBy(incident.getCreatedBy() != null ? incident.getCreatedBy().getEmail() : null);

		response.setAssignedTo(incident.getAssignedTo() != null ? incident.getAssignedTo().getEmail() : null);
		return response;
	}
}
