package org.example.team6backend.incident.controller;

import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.dto.IncidentResponse;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public IncidentResponse createIncident(@RequestBody IncidentRequest incidentRequest) {
        Incident incident = new Incident();
        incident.setSubject(incidentRequest.getSubject());
        incident.setDescription(incidentRequest.getDescription());
        incident.setIncidentCategory(incidentRequest.getIncidentCategory());

        Incident saved = incidentService.createIncident(incident);
        return IncidentResponse.fromEntity(saved);
    }

    @GetMapping
    public List<IncidentResponse> getAllIncidents() {
        return incidentService.findAll()
                .stream()
                .map(IncidentResponse::fromEntity)
                .toList();
    }
}
