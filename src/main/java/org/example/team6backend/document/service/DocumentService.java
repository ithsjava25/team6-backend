package org.example.team6backend.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.repository.DocumentRepository;
import org.example.team6backend.incident.entity.Incident;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

	private final S3Service s3Service;
	private final DocumentRepository documentRepository;

	/** Upload file */
	public Document uploadFile(MultipartFile file, Incident incident) {

		String fileKey = UUID.randomUUID() + "_" + file.getOriginalFilename();
		boolean uploaded = false;

		try {
			s3Service.uploadFile(fileKey, file);
			uploaded = true;

			Document document = new Document();
			document.setFileName(file.getOriginalFilename());
			document.setContentType(file.getContentType());
			document.setFileKey(fileKey);
			document.setFileSize(file.getSize());
			document.setIncident(incident);

			return documentRepository.save(document);

		} catch (Exception e) {
			if (uploaded) {
				try {
					s3Service.deleteFile(fileKey);
				} catch (Exception cleanupEx) {
					log.warn("Failed to cleanup S3 file: {}", fileKey, cleanupEx);
				}
			}
			throw new RuntimeException("File upload failed", e);
		}
	}

	/** Download file */
	public InputStream downloadFile(String objectKey) {
		return s3Service.downloadFile(objectKey);
	}

	/** Delete file */
	public void deleteFile(Document document) {
		s3Service.deleteFile(document.getFileKey());
		documentRepository.delete(document);
	}

	/** Fetch all files connected to one incident */
	public List<Document> getDocumentsByIncident(Incident incidentId) {
		return documentRepository.findByIncident(incidentId);
	}
}
