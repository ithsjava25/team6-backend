package org.example.team6backend.incident.service;

import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.activity.service.ActivityLogService;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.document.service.MinioService;
import org.example.team6backend.exception.ResourceNotFoundException;
import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.example.team6backend.incident.repository.IncidentRepository;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class IncidentService {

	private final IncidentRepository incidentRepository;
	private final ActivityLogService activityLogService;
	private final DocumentService documentService;
	private final AppUserRepository userRepository;
	private final NotificationService notificationService;
	private final MinioService minioService;
	private final AuditLogService auditLogService;

	public IncidentService(IncidentRepository incidentRepository, ActivityLogService activityLogService,
			DocumentService documentService, AppUserRepository userRepository, NotificationService notificationService,
			MinioService minioService, AuditLogService auditLogService) {
		this.incidentRepository = incidentRepository;
		this.activityLogService = activityLogService;
		this.documentService = documentService;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
		this.minioService = minioService;
		this.auditLogService = auditLogService;
	}

	/**
	 * Help-method for sorting
	 **/
	private Pageable withDefaultSort(Pageable pageable) {
		if (pageable.isUnpaged() || pageable.getSort().isSorted()) {
			return pageable;
		}
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
	}

	/**
	 * Create incident
	 **/
	@Transactional
	public Incident createIncident(IncidentRequest incidentRequest, List<MultipartFile> files, AppUser user) {

		List<String> uploadedKeys = new ArrayList<>();

		try {
			Incident incident = new Incident();
			incident.setSubject(incidentRequest.getSubject());
			incident.setDescription(incidentRequest.getDescription());
			incident.setIncidentCategory(incidentRequest.getIncidentCategory());
			incident.setCreatedBy(user);
			incident.setIncidentStatus(IncidentStatus.OPEN);
			incident.setCreatedAt(Instant.now());
			incident.setUpdatedAt(Instant.now());

			Incident savedIncident = incidentRepository.save(incident);

			if (files != null) {
				for (MultipartFile file : files) {
					if (!file.isEmpty()) {
						Document savedDocument = documentService.uploadFile(file, savedIncident, user);
						uploadedKeys.add(savedDocument.getFileKey());
					}
				}
			}
			activityLogService.log("INCIDENT_CREATED", user.getName() + " created incident.", savedIncident, user);

			auditLogService.log("CREATE_INCIDENT", user.getName() + " created incident #" + savedIncident.getId(), user,
					"Incident", savedIncident.getId().toString());

			return savedIncident;

		} catch (Exception e) {
			for (String key : uploadedKeys) {
				try {
					minioService.deleteFile(key);
				} catch (Exception cleanupEx) {
					log.warn("Failed rollback cleanup for fileKey: {}", key, cleanupEx);
				}
			}
			throw e;
		}
	}

	/**
	 * Find all incidents (Admin)
	 **/
	public Page<Incident> findAll(Pageable pageable) {
		return incidentRepository.findAll(withDefaultSort(pageable));
	}

	/**
	 * Find your own incidents (user)
	 **/
	public Page<Incident> findByCreatedBy(AppUser user, Pageable pageable) {
		return incidentRepository.findByCreatedBy(user, withDefaultSort(pageable));
	}

	/**
	 * Find assigned incidents per HANDLER
	 **/
	public Page<Incident> findByAssignedTo(AppUser user, Pageable pageable) {
		return incidentRepository.findByAssignedTo(user, withDefaultSort(pageable));
	}

	public Incident getById(Long id, AppUser user) {
		Incident incident = incidentRepository.findByIdWithDocuments(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not found"));

		if (user.getRole() == UserRole.PENDING) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed!");
		}

		boolean isAdmin = user.getRole() == UserRole.ADMIN;

		boolean isAssignedHandler = incident.getAssignedTo() != null
				&& incident.getAssignedTo().getId().equals(user.getId());

		boolean isOwner = incident.getCreatedBy() != null && incident.getCreatedBy().getId().equals(user.getId());

		if (isAdmin || isAssignedHandler || isOwner) {
			auditLogService.log("VIEW_INCIDENT", user.getName() + " viewed incident #" + incident.getId(), user,
					"Incident", incident.getId().toString());
			return incident;
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed!");
	}

	@Transactional
	public void deleteIncident(Long incidentId, AppUser currentUser) {

		Incident incident = incidentRepository.findByIdWithDocuments(incidentId)
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found!"));

		String incidentSubject = incident.getSubject();

		for (Document document : incident.getDocuments()) {
			try {
				minioService.deleteFile(document.getFileKey());
			} catch (Exception e) {
				log.warn("Failed to delete file from S3: " + document.getFileKey(), e);
			}
		}
		incidentRepository.delete(incident);

		auditLogService.log("DELETE_INCIDENT", currentUser.getName() + " deleted incident #" + incidentId, currentUser,
				"Incident", incidentId.toString());
	}

	@Transactional
	public Incident assignIncidentToHandler(Long incidentId, String handlerId, AppUser currentUser) {
		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

		if (incident.getIncidentStatus() == IncidentStatus.CLOSED) {
			throw new IllegalStateException("Cannot modify a closed incident. Status: " + incident.getIncidentStatus());
		}

		AppUser handler = userRepository.findById(handlerId)
				.orElseThrow(() -> new ResourceNotFoundException("Handler not found with id: " + handlerId));

		if (handler.getRole() != UserRole.HANDLER) {
			throw new IllegalStateException("User is not a handler. Role: " + handler.getRole());
		}

		AppUser oldHandler = incident.getAssignedTo();
		incident.setAssignedTo(handler);
		incident.setUpdatedAt(Instant.now());

		if (incident.getIncidentStatus() == IncidentStatus.OPEN) {
			incident.setIncidentStatus(IncidentStatus.IN_PROGRESS);
		}

		Incident savedIncident = incidentRepository.save(incident);

		String oldHandlerName = oldHandler != null ? oldHandler.getName() : "unassigned";
		activityLogService.log("INCIDENT_ASSIGNED",
				currentUser.getName() + " assigned incident from " + oldHandlerName + " to " + handler.getName(),
				savedIncident, currentUser);

		auditLogService.log("ASSIGN_INCIDENT",
				currentUser.getName() + " assigned incident #" + incidentId + " to " + handler.getName(), currentUser,
				"Incident", incidentId.toString());

		notificationService.createNotification("You have been assigned to an incident", handler, savedIncident);

		if (!handler.getId().equals(savedIncident.getCreatedBy().getId())) {
			notificationService.createNotification(handler.getName() + " has been assigned to your incident",
					savedIncident.getCreatedBy(), savedIncident);
		}

		log.info("Assigned incident {} to handler {} by admin {}", incidentId, handlerId, currentUser.getId());
		return savedIncident;
	}

	@Transactional
	public Incident updateIncidentStatus(Long incidentId, IncidentStatus newStatus, AppUser currentUser) {
		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

		IncidentStatus oldStatus = incident.getIncidentStatus();

		if (oldStatus == newStatus) {
			return incident;
		}

		incident.setIncidentStatus(newStatus);
		incident.setUpdatedAt(Instant.now());

		Incident savedIncident = incidentRepository.save(incident);

		activityLogService.log("STATUS_CHANGED",
				currentUser.getName() + " changed status from " + oldStatus + " to " + newStatus, savedIncident,
				currentUser);

		auditLogService.log("UPDATE_STATUS", currentUser.getName() + " changed incident #" + incidentId
				+ " status from " + oldStatus + " to " + newStatus, currentUser);

		if (savedIncident.getCreatedBy() != null && !savedIncident.getCreatedBy().getId().equals(currentUser.getId())) {

			notificationService.createNotification(
					"Your incident status was changed from " + oldStatus + " to " + newStatus,
					savedIncident.getCreatedBy(), savedIncident);
		}
		return incidentRepository.findByIdWithDocuments(savedIncident.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
	}

	public Incident unassignIncident(Long incidentId, AppUser currentUser) {
		if (currentUser.getRole() != UserRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can unassign incidents");
		}

		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

		if (incident.getIncidentStatus() == IncidentStatus.CLOSED) {
			throw new IllegalStateException("Cannot modify a closed incident. Status: " + incident.getIncidentStatus());
		}

		AppUser previousHandler = incident.getAssignedTo();

		if (previousHandler == null) {
			throw new IllegalStateException("Incident is not assigned to any handler");
		}

		incident.setAssignedTo(null);
		incident.setUpdatedAt(Instant.now());

		if (incident.getIncidentStatus() == IncidentStatus.IN_PROGRESS) {
			incident.setIncidentStatus(IncidentStatus.OPEN);
		}

		Incident savedIncident = incidentRepository.save(incident);

		activityLogService.log("INCIDENT_UNASSIGNED",
				currentUser.getName() + " unassigned incident from " + previousHandler.getName(), savedIncident,
				currentUser);

		auditLogService.log("UNASSIGN_INCIDENT",
				currentUser.getName() + " unassigned incident #" + incidentId + " from " + previousHandler.getName(),
				currentUser);

		notificationService.createNotification(
				"Incident #" + incident.getId() + " has been unassigned from you by " + currentUser.getName(),
				previousHandler, savedIncident);

		return savedIncident;
	}

	@Transactional
	public Incident closeIncident(Long incidentId, AppUser currentUser) {
		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

		if (currentUser.getRole() != UserRole.HANDLER && currentUser.getRole() != UserRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only handlers or admins can close incidents");
		}

		if (currentUser.getRole() != UserRole.ADMIN && (incident.getAssignedTo() == null
				|| !incident.getAssignedTo().getId().equals(currentUser.getId()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only close incidents assigned to you");
		}

		if (incident.getIncidentStatus() == IncidentStatus.CLOSED) {
			throw new IllegalStateException("Incident is already closed");
		}

		IncidentStatus oldStatus = incident.getIncidentStatus();

		incident.setIncidentStatus(IncidentStatus.CLOSED);
		incident.setUpdatedAt(Instant.now());

		Incident savedIncident = incidentRepository.save(incident);

		activityLogService.log("INCIDENT_CLOSED",
				currentUser.getName() + " closed incident (status changed from " + oldStatus + " to CLOSED)",
				savedIncident, currentUser);

		auditLogService.log("CLOSE_INCIDENT", currentUser.getName() + " closed incident #" + incidentId, currentUser);

		notificationService.createNotification(
				"Incident #" + incident.getId() + " has been closed by " + currentUser.getName(),
				savedIncident.getCreatedBy(), savedIncident);

		log.info("Closed incident {} by {} (role: {})", incidentId, currentUser.getId(), currentUser.getRole());
		return savedIncident;
	}

	@Transactional
	public Incident resolveIncident(Long incidentId, AppUser currentUser) {
		Incident incident = incidentRepository.findById(incidentId)
				.orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

		if (currentUser.getRole() != UserRole.HANDLER && currentUser.getRole() != UserRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only handlers or admins can resolve incidents");
		}

		if (currentUser.getRole() != UserRole.ADMIN && (incident.getAssignedTo() == null
				|| !incident.getAssignedTo().getId().equals(currentUser.getId()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only resolve incidents assigned to you");
		}

		if (incident.getIncidentStatus() == IncidentStatus.CLOSED) {
			throw new IllegalStateException("Cannot resolve a closed incident");
		}

		if (incident.getIncidentStatus() == IncidentStatus.RESOLVED) {
			throw new IllegalStateException("Incident is already resolved");
		}

		IncidentStatus oldStatus = incident.getIncidentStatus();
		incident.setIncidentStatus(IncidentStatus.RESOLVED);
		incident.setUpdatedAt(Instant.now());

		Incident savedIncident = incidentRepository.save(incident);

		activityLogService.log("INCIDENT_RESOLVED",
				currentUser.getName() + " resolved incident (status changed from " + oldStatus + " to RESOLVED)",
				savedIncident, currentUser);

		auditLogService.log("RESOLVE_INCIDENT", currentUser.getName() + " resolved incident #" + incidentId,
				currentUser);

		notificationService.createNotification(
				"Incident #" + incident.getId() + " has been marked as resolved by " + currentUser.getName(),
				savedIncident.getCreatedBy(), savedIncident);

		log.info("Resolved incident {} by {} (role: {})", incidentId, currentUser.getId(), currentUser.getRole());
		return savedIncident;
	}
}
