package org.example.team6backend.incident.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.user.entity.AppUser;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
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
}
