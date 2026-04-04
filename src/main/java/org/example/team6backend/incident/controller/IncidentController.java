package org.example.team6backend.incident.controller;

import jakarta.validation.Valid;
import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.dto.IncidentResponse;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /**Create new incident*/
    @PostMapping
    @PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN')")
    public IncidentResponse createIncident(@Valid @RequestBody IncidentRequest incidentRequest) {
        Incident incident = new Incident();
        incident.setSubject(incidentRequest.getSubject());
        incident.setDescription(incidentRequest.getDescription());
        incident.setIncidentCategory(incidentRequest.getIncidentCategory());

        Incident saved = incidentService.createIncident(incident);
        return IncidentResponse.fromEntity(saved);
    }

    /**Get my incidents(user)*/
    @PreAuthorize("hasRole('RESIDENT')")
    @GetMapping ("/my")
    public Page<IncidentResponse> getMyIncidents(Pageable pageable){
        return incidentService.findByCreatedBy(pageable)
                .map(IncidentResponse::fromEntity);
    }

    @PreAuthorize("hasRole('HANDLER')")
    @GetMapping ("/assigned")
    public Page<IncidentResponse> getAssignedIncidents(Pageable pageable){
        return incidentService.findByAssignedTo(pageable)
                .map(IncidentResponse::fromEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
        return incidentService.findAll(pageable)
                .map(IncidentResponse::fromEntity);
    }
}
