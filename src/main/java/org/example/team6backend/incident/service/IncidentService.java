package org.example.team6backend.incident.service;

import org.example.team6backend.activity.service.ActivityLogService;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.exception.ResourceNotFoundException;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.example.team6backend.incident.repository.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidentService {

	private final IncidentRepository incidentRepository;
	private final ActivityLogService activityLogService;
	private final DocumentService documentService;

	public IncidentService(IncidentRepository incidentRepository, ActivityLogService activityLogService,
			DocumentService documentService) {
		this.incidentRepository = incidentRepository;
		this.activityLogService = activityLogService;
		this.documentService = documentService;
	}

	/** Help-method for sorting **/
	private Pageable withDefaultSort(Pageable pageable) {
		if (pageable.isUnpaged() || pageable.getSort().isSorted()) {
			return pageable;
		}
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
	}

	/** Create incident **/
	public Incident createIncident(Incident incident) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
		AppUser appUser = userDetails.getUser();

		incident.setCreatedBy(appUser);
		incident.setIncidentStatus(IncidentStatus.OPEN);
		incident.setCreatedAt(LocalDateTime.now());
		incident.setUpdatedAt(LocalDateTime.now());

		Incident savedIncident = incidentRepository.save(incident);

		activityLogService.log("INCIDENT_CREATED", appUser.getName() + " created the incident", savedIncident, appUser);

		return savedIncident;
	}

	/** Find all incidents (Admin) **/
	public Page<Incident> findAll(Pageable pageable) {
		return incidentRepository.findAll(withDefaultSort(pageable));
	}

	/** Find your own incidents (user) **/
	public Page<Incident> findByCreatedBy(AppUser user, Pageable pageable) {
		return incidentRepository.findByCreatedBy(user, withDefaultSort(pageable));
	}

	/** Find assigned incidents per HANDLER **/
	public Page<Incident> findByAssignedTo(AppUser user, Pageable pageable) {
		return incidentRepository.findByAssignedTo(user, withDefaultSort(pageable));
	}

	public Incident getById(Long id, AppUser user) {
		Incident incident = incidentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not found"));

		boolean isAdmin = user.getRole().name().equals("ADMIN");
		boolean isHandler = incident.getAssignedTo() != null && incident.getAssignedTo().getId().equals(user.getId());
		boolean isResident = incident.getCreatedBy().getId().equals(user.getId());

		if (!isAdmin && !isHandler && !isResident) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return incident;
	}
	public void deleteIncident(Incident incident) {
		List<Document> documents = documentService.getDocumentsByIncident(incident);
		for (Document document : documents) {
			documentService.deleteFile(document);
		}
		incidentRepository.delete(incident);
	}
}
