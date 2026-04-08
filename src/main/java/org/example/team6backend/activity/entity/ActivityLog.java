package org.example.team6backend.activity.entity;

import jakarta.persistence.*;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
public class ActivityLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String action;
	private String description;
	private LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name = "incident_id", nullable = false)
	private Incident incident;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@PrePersist
	void onCreated() {
		createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Incident getIncident() {
		return incident;
	}

	public void setIncident(Incident incident) {
		this.incident = incident;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}
}
