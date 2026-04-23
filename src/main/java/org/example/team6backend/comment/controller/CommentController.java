package org.example.team6backend.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.comment.dto.CommentRequest;
import org.example.team6backend.comment.dto.CommentResponse;
import org.example.team6backend.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/incident/{incidentId}")
	public ResponseEntity<List<CommentResponse>> getCommentByIncidentId(@PathVariable Long incidentId) {
		log.info("GET /comments/incident/{} - Fetching comments", incidentId);
		List<CommentResponse> comments = commentService.getCommentByIncidentId(incidentId).stream()
				.map(CommentResponse::fromEntity).toList();
		return ResponseEntity.ok(comments);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
		log.info("POST /comments - Creating comment for incident: {}", request.getIncidentId());
		var comment = commentService.createComment(request.getIncidentId(), request.getUserId(), request.getMessage());
		return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.fromEntity(comment));
	}
}
