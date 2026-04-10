package org.example.team6backend.document.entity;

import jakarta.persistence.*;
import org.example.team6backend.incident.entity.Incident;

@Entity
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

	public Long getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFileKey() {
		return fileKey;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public Incident getIncident() {
		return incident;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setIncident(Incident incident) {
		this.incident = incident;
	}
}
