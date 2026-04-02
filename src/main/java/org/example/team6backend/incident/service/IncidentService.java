package org.example.team6backend.incident.service;

import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.example.team6backend.incident.repository.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**Help-method for sorting**/
    private Pageable withDefaultSort(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    /**Create incident**/
    public Incident createIncident(Incident incident){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        AppUser appUser = userDetails.getUser();

        incident.setCreatedBy(appUser);
        incident.setIncidentStatus(IncidentStatus.OPEN);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    /**Find all incidents (Admin)**/
    public Page <Incident> findAll(Pageable pageable){
        return incidentRepository.findAll(withDefaultSort(pageable));
    }

    /**Find your own incidents (user)**/
    public Page<Incident> findByCreatedBy(Pageable pageable){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        AppUser user = userDetails.getUser();

        return incidentRepository.findByCreatedBy(user, withDefaultSort(pageable));
    }

    /**Find assigned incidents per HANDLER**/
    public Page<Incident> findByAssignedTo(Pageable pageable){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        AppUser user = userDetails.getUser();

        return incidentRepository.findByAssignedTo(user, withDefaultSort(pageable));
    }
}
