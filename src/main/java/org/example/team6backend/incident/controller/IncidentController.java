package org.example.team6backend.incident.controller;

import jakarta.validation.Valid;
import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.dto.IncidentResponse;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

	private final IncidentService incidentService;

	public IncidentController(IncidentService incidentService) {
		this.incidentService = incidentService;
	}

	/** Create new incident */
	@PostMapping
	@PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN')")
	public IncidentResponse createIncident(@RequestBody @Valid IncidentRequest incidentRequest) {
		Incident incident = new Incident();
		incident.setSubject(incidentRequest.getSubject());
		incident.setDescription(incidentRequest.getDescription());
		incident.setIncidentCategory(incidentRequest.getIncidentCategory());

		Incident saved = incidentService.createIncident(incident);
		return IncidentResponse.fromEntity(saved);
	}

	/** Get my incidents(user) */
	@PreAuthorize("hasRole('RESIDENT')")
	@GetMapping("/my")
	public Page<IncidentResponse> getMyIncidents(@AuthenticationPrincipal CustomUserDetails userDetails,
			Pageable pageable) {

		AppUser user = userDetails.getUser();
		return incidentService.findByCreatedBy(user, pageable).map(IncidentResponse::fromEntity);
	}

	/** Get assigned incidents(handler) */
	@PreAuthorize("hasRole('HANDLER')")
	@GetMapping("/assigned")
	public Page<IncidentResponse> getAssignedIncidents(@AuthenticationPrincipal CustomUserDetails userDetails,
			Pageable pageable) {
		AppUser user = userDetails.getUser();
		return incidentService.findByAssignedTo(user, pageable).map(IncidentResponse::fromEntity);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/all")
	public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
		return incidentService.findAll(pageable).map(IncidentResponse::fromEntity);
	}

	@PreAuthorize("hasAnyRole('RESIDENT', 'HANDLER', 'ADMIN')")
	@GetMapping("/{id}")
	public IncidentResponse getIncidentById(@PathVariable Long id) {
		return IncidentResponse.fromEntity(incidentService.getById(id));
	}
}
