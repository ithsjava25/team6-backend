package org.example.team6backend.incident.entity;

import jakarta.persistence.*;
import org.example.team6backend.user.entity.AppUser;

import java.time.LocalDateTime;

@Entity
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String description;

    @Enumerated(EnumType.STRING)
    private IncidentCategory incidentCategory;

    @Enumerated(EnumType.STRING)
    private IncidentStatus incidentStatus;

    @ManyToOne
    private AppUser createdBy;

    @ManyToOne
    private AppUser modifiedBy;

    @ManyToOne
    private AppUser assignedTo;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public IncidentCategory getIncidentCategory() {
        return incidentCategory;
    }

    public IncidentStatus getIncidentStatus() {
        return incidentStatus;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public AppUser getModifiedBy() {
        return modifiedBy;
    }

    public AppUser getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIncidentCategory(IncidentCategory incidentCategory) {
        this.incidentCategory = incidentCategory;
    }

    public void setIncidentStatus(IncidentStatus incidentStatus) {
        this.incidentStatus = incidentStatus;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }

    public void setModifiedBy(AppUser modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void setAssignedTo(AppUser assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
