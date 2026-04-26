package org.example.team6backend.document.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.document.dto.DocumentDTO;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.document.service.MinioService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

	private final DocumentService documentService;
	private final IncidentService incidentService;
	private final MinioService minioService;

	private AppUser getUser(CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		return userDetails.getUser();
	}

	@GetMapping("/{fileKey}")
	public ResponseEntity<Resource> getFile(@PathVariable String fileKey,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		log.info("GET /documents/{} - Fetching file", fileKey);
		AppUser user = getUser(userDetails);

		Document document = documentService.getByFileKey(fileKey)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		Incident incident = incidentService.getById(document.getIncident().getId(), user);
		if (incident == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		InputStream inputStream = documentService.downloadFile(fileKey, user);
		MediaType mediaType = document.getContentType() != null
				? MediaType.parseMediaType(document.getContentType())
				: MediaType.APPLICATION_OCTET_STREAM;

		return ResponseEntity.ok().contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
				.body(new InputStreamResource(inputStream));
	}

	@PostMapping("/upload/{incidentId}")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<List<DocumentDTO>> uploadFile(@PathVariable Long incidentId,
			@RequestParam("files") List<MultipartFile> files, @AuthenticationPrincipal CustomUserDetails userDetails) {

		log.info("POST /documents/upload/{} - Uploading {} files", incidentId, files.size());
		AppUser user = getUser(userDetails);
		Incident incident = incidentService.getById(incidentId, user);
		List<DocumentDTO> uploadedDocs = new ArrayList<>();

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				Document doc = documentService.uploadFile(file, incident, user);
				DocumentDTO dto = new DocumentDTO();
				dto.setFileName(doc.getFileName());
				dto.setFileKey(doc.getFileKey());
				dto.setContentType(doc.getContentType());
				dto.setFileSize(doc.getFileSize());
				dto.setImage(doc.getContentType() != null && doc.getContentType().startsWith("image/"));
				uploadedDocs.add(dto);
				log.debug("Uploaded file: {}", doc.getFileName());
			}
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocs);
	}

	@DeleteMapping("/{documentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> deleteFile(@PathVariable Long documentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		log.info("DELETE /documents/{} - Deleting file", documentId);
		AppUser user = getUser(userDetails);

		Document document = documentService.getById(documentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		documentService.deleteFile(document, user);
		return ResponseEntity.noContent().build();
	}
}
