package org.example.team6backend.incident.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.incident.dto.AssignIncidentRequest;
import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.dto.IncidentResponse;
import org.example.team6backend.incident.dto.UpdateIncidentStatusRequest;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentCategory;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN')")
    public ResponseEntity<IncidentResponse> createIncidentWithFiles(
            @RequestParam("subject") String subject,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("incidentCategory") IncidentCategory incidentCategory,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        log.info("POST /api/incidents - Creating new incident with {} files", files != null ? files.size() : 0);
        AppUser user = customUserDetails.getUser();

        IncidentRequest incidentRequest = new IncidentRequest();
        incidentRequest.setSubject(subject);
        incidentRequest.setDescription(description);
        incidentRequest.setIncidentCategory(incidentCategory);

        Incident saved = incidentService.createIncident(incidentRequest, files, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(IncidentResponse.fromEntityBasic(saved));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN')")
    public ResponseEntity<IncidentResponse> createIncident(
            @RequestBody @Valid IncidentRequest incidentRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        log.info("POST /api/incidents (JSON) - Creating new incident");
        AppUser user = customUserDetails.getUser();
        Incident saved = incidentService.createIncident(incidentRequest, null, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(IncidentResponse.fromEntityBasic(saved));
    }

    @PreAuthorize("hasRole('RESIDENT')")
    @GetMapping("/my")
    public ResponseEntity<Page<IncidentResponse>> getMyIncidents(
            @AuthenticationPrincipal CustomUserDetails userDetails, Pageable pageable) {
        log.info("GET /api/incidents/my - Fetching my incidents");
        AppUser user = userDetails.getUser();
        return ResponseEntity.ok(incidentService.findByCreatedBy(user, pageable)
                .map(IncidentResponse::fromEntityBasic));
    }

    @PreAuthorize("hasRole('HANDLER')")
    @GetMapping("/assigned")
    public ResponseEntity<Page<IncidentResponse>> getAssignedIncidents(
            @AuthenticationPrincipal CustomUserDetails userDetails, Pageable pageable) {
        log.info("GET /api/incidents/assigned - Fetching assigned incidents");
        AppUser user = userDetails.getUser();
        return ResponseEntity.ok(incidentService.findByAssignedTo(user, pageable)
                .map(IncidentResponse::fromEntityBasic));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<IncidentResponse>> getAllIncidents(Pageable pageable) {
        log.info("GET /api/incidents/all - Fetching all incidents");
        return ResponseEntity.ok(incidentService.findAll(pageable)
                .map(IncidentResponse::fromEntityBasic));
    }

    @PreAuthorize("hasAnyRole('RESIDENT', 'HANDLER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable Long id) {
        log.info("GET /api/incidents/{} - Fetching incident", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        AppUser currentUser = userDetails.getUser();
        notificationService.markNotificationAsReadForIncident(currentUser.getId(), id);

        return ResponseEntity.ok(IncidentResponse.fromEntityWithDocuments(
                incidentService.getById(id, currentUser)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{incidentId}/assign")
    public ResponseEntity<IncidentResponse> assignIncident(@PathVariable Long incidentId,
                                                           @Valid @RequestBody AssignIncidentRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails adminUser) {
        log.info("PATCH /api/incidents/{}/assign - Assigning to handler {}", incidentId, request.handlerId());
        Incident updatedIncident = incidentService.assignIncidentToHandler(incidentId, request.handlerId(),
                adminUser.getUser());
        return ResponseEntity.ok(IncidentResponse.fromEntityBasic(updatedIncident));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{incidentId}/unassign")
    public ResponseEntity<IncidentResponse> unassignIncident(@PathVariable Long incidentId,
                                                             @AuthenticationPrincipal CustomUserDetails adminUser) {
        log.info("PATCH /api/incidents/{}/unassign - Unassigning incident", incidentId);
        Incident updatedIncident = incidentService.unassignIncident(incidentId, adminUser.getUser());
        return ResponseEntity.ok(IncidentResponse.fromEntityBasic(updatedIncident));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HANDLER')")
    @PatchMapping("/{incidentId}/close")
    public ResponseEntity<IncidentResponse> closeIncident(@PathVariable Long incidentId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("PATCH /api/incidents/{}/close - Closing incident", incidentId);
        Incident closedIncident = incidentService.closeIncident(incidentId, userDetails.getUser());
        return ResponseEntity.ok(IncidentResponse.fromEntityBasic(closedIncident));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HANDLER')")
    @PatchMapping("/{incidentId}/resolve")
    public ResponseEntity<IncidentResponse> resolveIncident(@PathVariable Long incidentId,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("PATCH /api/incidents/{}/resolve - Resolving incident", incidentId);
        Incident resolvedIncident = incidentService.resolveIncident(incidentId, userDetails.getUser());
        return ResponseEntity.ok(IncidentResponse.fromEntityBasic(resolvedIncident));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{incidentId}/status")
    public ResponseEntity<IncidentResponse> updateStatus(@PathVariable Long incidentId,
                                                         @Valid @RequestBody UpdateIncidentStatusRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails adminUser) {
        log.info("PATCH /api/incidents/{}/status - Updating status to {}", incidentId, request.status());
        Incident updatedIncident = incidentService.updateIncidentStatus(incidentId, request.status(),
                adminUser.getUser());
        return ResponseEntity.ok(IncidentResponse.fromEntity(updatedIncident));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/handlers")
    public ResponseEntity<List<UserResponse>> getAvailableHandlers() {
        log.info("GET /api/incidents/handlers - Fetching all handlers");
        List<AppUser> handlers = userService.getUsersByRole(UserRole.HANDLER);
        return ResponseEntity.ok(handlers.stream().map(userMapper::toResponse).toList());
    }
}