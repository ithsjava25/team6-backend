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

	@Column(name = "incident_category")
	@Enumerated(EnumType.STRING)
	private IncidentCategory incidentCategory;

	@Column(name = "incident_status")
	@Enumerated(EnumType.STRING)
	private IncidentStatus incidentStatus;

	@ManyToOne
	@JoinColumn(name = "created_by_id")
	private AppUser createdBy;

	@ManyToOne
	@JoinColumn(name = "modified_by_id")
	private AppUser modifiedBy;

	@ManyToOne
	@JoinColumn(name = "assigned_to_id")
	private AppUser assignedTo;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
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

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
