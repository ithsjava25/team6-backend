package org.example.team6backend.document.controller;

import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/documents")
public class DocumentController {

	private final DocumentService documentService;
	private final IncidentService incidentService;

	public DocumentController(DocumentService documentService, IncidentService incidentService) {
		this.documentService = documentService;
		this.incidentService = incidentService;
	}

	@PostMapping("/upload/{incidentId}")
	public String uploadFile(@PathVariable Long incidentId, @RequestParam("files") List<MultipartFile> files,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		AppUser user = userDetails.getUser();
		Incident incident = incidentService.getById(incidentId, user);

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				documentService.uploadFile(file, incident);
			}
		}
		return "redirect:/incidents/" + incidentId;
	}

	@GetMapping("/download/{incidentId}")
	public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long incidentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		AppUser user = userDetails.getUser();
		Incident incident = incidentService.getById(incidentId, user);

		List<Document> documents = documentService.getDocumentsByIncident(incident);
		if (documents.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Document document = documents.get(0);

		InputStream stream = documentService.downloadFile(document.getFileKey());

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
				.contentType(MediaType.parseMediaType(document.getContentType())).body(new InputStreamResource(stream));

	}
}
