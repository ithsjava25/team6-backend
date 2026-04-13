package org.example.team6backend.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.repository.DocumentRepository;
import org.example.team6backend.incident.entity.Incident;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

	/** Upload file */
	public Document uploadFile(MultipartFile file, Incident incident) {

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

			return documentRepository.save(document);

		} catch (Exception e) {
			if (uploaded) {
				try {
					minioService.deleteFile(fileKey);
				} catch (Exception cleanupEx) {
					log.warn("Failed to cleanup S3 file: {}", fileKey, cleanupEx);
				}
			}
			throw new RuntimeException("File upload failed", e);
		}
	}

	/** Download file */
	public InputStream downloadFile(String objectKey) {
		return minioService.downloadFile(objectKey);
	}

	/** Delete file */
    @Transactional
	public void deleteFile(Document document) {
		documentRepository.delete(document);

        try {
            minioService.deleteFile(document.getFileKey());
        } catch (Exception e) {
            log.warn("Could not delete file: {}", document.getFileKey(), e);
        }
	}

	/** Fetch all files connected to one incident */
	public List<Document> getDocumentsByIncident(Incident incidentId) {
		return documentRepository.findByIncident(incidentId);
	}
    public Optional<Document> getByFileKey(String fileKey) {
        return documentRepository.findByFileKey(fileKey);
    }
}
