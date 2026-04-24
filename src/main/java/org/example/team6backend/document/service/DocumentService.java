package org.example.team6backend.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.repository.DocumentRepository;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

	private final MinioService minioService;
	private final DocumentRepository documentRepository;
	private final AuditLogService auditLogService;

	/** Upload file */
	@Transactional
	public Document uploadFile(MultipartFile file, Incident incident, AppUser user) {
		String fileKey = UUID.randomUUID() + "_" + file.getOriginalFilename();
		boolean uploaded = false;

		try {
			minioService.uploadFile(fileKey, file);
			uploaded = true;

			Document document = new Document();
			document.setFileName(file.getOriginalFilename());
			document.setContentType(file.getContentType());
			document.setFileKey(fileKey);
			document.setFileSize(file.getSize());
			document.setIncident(incident);

			Document savedDocument = documentRepository.save(document);

			auditLogService.log("UPLOAD_DOCUMENT",
					user.getName() + " uploaded '" + file.getOriginalFilename() + "' to incident #" + incident.getId(),
					user);
			return savedDocument;

		} catch (Exception e) {
			if (uploaded) {
				try {
					minioService.deleteFile(fileKey);
				} catch (Exception cleanupEx) {
					log.warn("Failed to cleanup Minio file: {}", fileKey, cleanupEx);
				}
			}
			throw new RuntimeException("File upload failed", e);
		}
	}

	/** Download file */
	@Transactional
	public InputStream downloadFile(String objectKey, AppUser user) {
		try {
			InputStream stream = minioService.downloadFile(objectKey);

			auditLogService.log("DOWNLOAD_DOCUMENT", user.getName() + " downloaded file '" + objectKey + "'", user);

			return stream;

		} catch (MinioService.FileMissingException e) {
			log.warn("Missing file in Minio: {}", objectKey, e);

			var docOpt = documentRepository.findByFileKey(objectKey);

			if (docOpt.isPresent()) {
				log.warn("FOUND document in DB → deleting: {}", objectKey);
				documentRepository.delete(docOpt.get());
			} else {
				log.warn("NO document found in DB for: {}", objectKey);
			}

			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");

		} catch (Exception e) {
			log.error("Failed to download file from Minio: {}", objectKey, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download file");
		}
	}

	/** Delete file */
	@Transactional
	public void deleteFile(Document document, AppUser user) {
		String fileName = document.getFileName();
		String fileKey = document.getFileKey();

		documentRepository.delete(document);

		try {
			minioService.deleteFile(document.getFileKey());
		} catch (Exception e) {
			log.warn("Could not delete file: {}", document.getFileKey(), e);
		}
		auditLogService.log("DELETE_DOCUMENT", user.getName() + " deleted file '" + fileName + "'", user);
	}

	/** Fetch all files connected to one incident */
	public List<Document> getDocumentsByIncident(Incident incident) {
		return documentRepository.findByIncident(incident);
	}

	/** Fetch document by fileKey */
	public Optional<Document> getByFileKey(String fileKey) {
		return documentRepository.findByFileKey(fileKey);
	}

	/** Fetch document by ID */
	public Optional<Document> getById(Long documentId) {
		return documentRepository.findById(documentId);
	}

	/** Fetch documents by incident ID */
	public List<Document> getDocumentsByIncidentId(Long incidentId) {
		return documentRepository.findByIncidentId(incidentId);
	}
}
