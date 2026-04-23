package org.example.team6backend.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.team6backend.incident.entity.IncidentCategory;

@Data
public class IncidentRequest {
    @NotBlank(message = "Subject is required")
    private String subject;

    private String description;

    @NotNull(message = "Incident category is required")
    private IncidentCategory incidentCategory;
}