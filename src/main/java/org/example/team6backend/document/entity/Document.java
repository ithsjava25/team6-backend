package org.example.team6backend.document.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.team6backend.incident.entity.Incident;

@Entity
@Getter
@Setter
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "file_name")
	private String fileName;
	@Column(name = "content_type")
	private String contentType;
	@Column(name = "file_key")
	private String fileKey;
	@Column(name = "file_size")
	private Long fileSize;

	@ManyToOne
	@JoinColumn(name = "incident_id")
	private Incident incident;
}
