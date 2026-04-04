package org.example.team6backend.comment.controller;

import jakarta.validation.Valid;
import org.example.team6backend.comment.dto.CommentRequest;
import org.example.team6backend.comment.entity.Comment;
import org.example.team6backend.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/incident/{incidentId}")
	public ResponseEntity<List<Comment>> getCommentByIncidentId(@PathVariable Long incidentId) {
		List<Comment> comments = commentService.getCommentByIncidentId(incidentId);
		return ResponseEntity.ok(comments);
	}

	@PostMapping
	public ResponseEntity<Comment> createComment(@Valid @RequestBody CommentRequest request) {
		Comment saveComment = commentService.createComment(request.getIncidentId(), request.getUserId(),
				request.getMessage());
		return ResponseEntity.ok(saveComment);
	}
}
