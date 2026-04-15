package org.example.team6backend.incident.entity;

import jakarta.persistence.*;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.user.entity.AppUser;

import java.time.Instant;
import java.util.List;

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
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Document> documents;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
		updatedAt = Instant.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<Document> getDocuments() {
		return documents;
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

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
}
