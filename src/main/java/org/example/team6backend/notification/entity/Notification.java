package org.example.team6backend.notification.entity;

import jakarta.persistence.*;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;

import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String message;

	@Column(name = "is_read", nullable = false)
	private boolean read = false;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne
	@JoinColumn(name = "incident_id", nullable = false)
	private Incident incident;

	@PrePersist
	public void onCreate() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

	public Incident getIncident() {
		return incident;
	}

	public void setIncident(Incident incident) {
		this.incident = incident;
	}
}
