package org.example.team6backend.auditlog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.team6backend.user.entity.AppUser;

import java.time.Instant;

@Entity
@Getter
@Setter
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String userName;
	private String action;
	private String targetType;
	private String targetId;
	private String details;
	private Instant createdAt;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser performedBy;
}
