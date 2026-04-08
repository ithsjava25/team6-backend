package org.example.team6backend.incident.service;

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

@Service
public class IncidentService {

	private final IncidentRepository incidentRepository;

	public IncidentService(IncidentRepository incidentRepository) {
		this.incidentRepository = incidentRepository;
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

		return incidentRepository.save(incident);
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
}
