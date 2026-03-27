package org.example.team6backend.incident.dto;

import lombok.Data;
import org.example.team6backend.incident.entity.IncidentCategory;

@Data
public class IncidentRequest {

    private String subject;
    private String description;
    private IncidentCategory incidentCategory;
}
