package org.example.team6backend.incident.dto;

import jakarta.validation.constraints.NotNull;
import org.example.team6backend.incident.entity.IncidentStatus;

public record UpdateIncidentStatusRequest (@NotNull IncidentStatus status){
}
