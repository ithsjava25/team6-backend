package org.example.team6backend.comment.controller;

import jakarta.validation.Valid;
import org.example.team6backend.comment.dto.CommentRequest;
import org.example.team6backend.comment.dto.CommentResponse;
import org.example.team6backend.comment.entity.Comment;
import org.example.team6backend.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/comments")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/incident/{incidentId}")
	@ResponseBody
	public ResponseEntity<List<CommentResponse>> getCommentByIncidentId(@PathVariable Long incidentId) {
		List<CommentResponse> comments = commentService.getCommentByIncidentId(incidentId)
				.stream()
				.map(CommentResponse::fromEntity)
				.toList();

		return ResponseEntity.ok(comments);
	}

	@PostMapping
	public String createComment(@Valid CommentRequest request) {
		commentService.createComment(request.getIncidentId(), request.getUserId(), request.getMessage());
		return "redirect:/incidents/" + request.getIncidentId();
	}
}
