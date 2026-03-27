package org.example.team6backend.incident.service;

import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.example.team6backend.incident.repository.IncidentRepository;
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

    public Incident createIncident(Incident incident){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        AppUser appUser = userDetails.getUser();

        incident.setCreatedBy(appUser);

        incident.setIncidentStatus(IncidentStatus.OPEN);
        incident.setCreatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    public List<Incident> findByCreatedBy(AppUser user){
        return incidentRepository.findByCreatedBy(user);
    }

    public List<Incident> findByAssignedTo(AppUser user){
        return incidentRepository.findByAssignedTo(user);
    }

    public List <Incident> findAll(){
        return incidentRepository.findAll();
    }
}
