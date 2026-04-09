package org.example.team6backend.incident.dto;

import jakarta.validation.constraints.NotNull;

public record AssignIncidentRequest(@NotNull(message = "Handler ID is required") String handlerId) {
}