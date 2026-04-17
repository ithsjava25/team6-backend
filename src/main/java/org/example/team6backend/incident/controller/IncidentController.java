package org.example.team6backend.incident.controller;

import jakarta.validation.Valid;
import org.example.team6backend.incident.dto.AssignIncidentRequest;
import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.dto.IncidentResponse;
import org.example.team6backend.incident.dto.UpdateIncidentStatusRequest;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.dto.UserResponse;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.mapper.UserMapper;
import org.example.team6backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

	private final IncidentService incidentService;
	private final UserService userService;
	private final UserMapper userMapper;
	private final NotificationService notificationService;

	public IncidentController(IncidentService incidentService, UserService userService, UserMapper userMapper,
			NotificationService notificationService) {
		this.incidentService = incidentService;
		this.userService = userService;
		this.userMapper = userMapper;
		this.notificationService = notificationService;
	}

	/** Create new incident */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN')")
	public IncidentResponse createIncident(@RequestBody @Valid IncidentRequest incidentRequest,
			@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		AppUser user = customUserDetails.getUser();

		Incident saved = incidentService.createIncident(incidentRequest, null, user);

		return IncidentResponse.fromEntityBasic(saved);
	}

	/** Get my incidents(user) */
	@PreAuthorize("hasRole('RESIDENT')")
	@GetMapping("/my")
	public Page<IncidentResponse> getMyIncidents(@AuthenticationPrincipal CustomUserDetails userDetails,
			Pageable pageable) {

		AppUser user = userDetails.getUser();
		return incidentService.findByCreatedBy(user, pageable).map(IncidentResponse::fromEntityBasic);
	}

	/** Get assigned incidents(handler) */
	@PreAuthorize("hasRole('HANDLER')")
	@GetMapping("/assigned")
	public Page<IncidentResponse> getAssignedIncidents(@AuthenticationPrincipal CustomUserDetails userDetails,
			Pageable pageable) {
		AppUser user = userDetails.getUser();
		return incidentService.findByAssignedTo(user, pageable).map(IncidentResponse::fromEntityBasic);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/all")
	public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
		return incidentService.findAll(pageable).map(IncidentResponse::fromEntityBasic);
	}

	@PreAuthorize("hasAnyRole('RESIDENT', 'HANDLER', 'ADMIN')")
	@GetMapping("/{id}")
	public IncidentResponse getIncidentById(@PathVariable Long id) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		AppUser currentUser = userDetails.getUser();

		notificationService.markNotificationAsReadForIncident(currentUser.getId(), id);

		return IncidentResponse.fromEntityWithDocuments(incidentService.getById(id, currentUser));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{incidentId}/assign")
	public IncidentResponse assignIncident(@PathVariable Long incidentId,
			@Valid @RequestBody AssignIncidentRequest request, @AuthenticationPrincipal CustomUserDetails adminUser) {
		Incident updatedIncident = incidentService.assignIncidentToHandler(incidentId, request.handlerId(),
				adminUser.getUser());
		return IncidentResponse.fromEntityBasic(updatedIncident);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{incidentId}/unassign")
	public ResponseEntity<IncidentResponse> unassignIncident(@PathVariable Long incidentId,
			@AuthenticationPrincipal CustomUserDetails adminUser) {
		Incident updatedIncident = incidentService.unassignIncident(incidentId, adminUser.getUser());
		return ResponseEntity.ok(IncidentResponse.fromEntityBasic(updatedIncident));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'HANDLER')")
	@PatchMapping("/{incidentId}/close")
	public ResponseEntity<IncidentResponse> closeIncident(@PathVariable Long incidentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Incident closedIncident = incidentService.closeIncident(incidentId, userDetails.getUser());
		return ResponseEntity.ok(IncidentResponse.fromEntityBasic(closedIncident));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'HANDLER')")
	@PatchMapping("/{incidentId}/resolve")
	public ResponseEntity<IncidentResponse> resolveIncident(@PathVariable Long incidentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Incident resolvedIncident = incidentService.resolveIncident(incidentId, userDetails.getUser());
		return ResponseEntity.ok(IncidentResponse.fromEntityBasic(resolvedIncident));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{incidentId}/status")
	public IncidentResponse updateStatus(@PathVariable Long incidentId,
			@Valid @RequestBody UpdateIncidentStatusRequest request,
			@AuthenticationPrincipal CustomUserDetails adminUser) {

		Incident updateIncident = incidentService.updateIncidentStatus(incidentId, request.status(),
				adminUser.getUser());

		return IncidentResponse.fromEntity(updateIncident);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/handlers")
	public ResponseEntity<List<UserResponse>> getAvailableHandlers() {
		List<AppUser> handlers = userService.getUsersByRole(UserRole.HANDLER);
		return ResponseEntity.ok(handlers.stream().map(userMapper::toResponse).toList());
	}
}
