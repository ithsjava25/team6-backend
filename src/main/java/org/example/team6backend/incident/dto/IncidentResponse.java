package org.example.team6backend.incident.dto;

import lombok.Data;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentCategory;
import org.example.team6backend.incident.entity.IncidentStatus;

import java.time.LocalDateTime;

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

    public static IncidentResponse fromEntity(Incident incident) {
        IncidentResponse response = new IncidentResponse();

        response.setId(incident.getId());
        response.setSubject(incident.getSubject());
        response.setDescription(incident.getDescription());
        response.setIncidentStatus(incident.getIncidentStatus());
        response.setIncidentCategory(incident.getIncidentCategory());
        response.setCreatedAt(incident.getCreatedAt());

        response.setCreatedBy(
                incident.getCreatedBy() != null ? incident.getCreatedBy().getEmail() : null
        );

        response.setAssignedTo(
                incident.getAssignedTo() != null ? incident.getAssignedTo().getEmail() : null
        );
        return response;
    }
}
