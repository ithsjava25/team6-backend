package org.example.team6backend.comment.dto;

import org.example.team6backend.comment.entity.Comment;

import java.time.Instant;

public record CommentResponse(String id, String message, Instant createdAt, CommentUserResponse user

) {
	public static CommentResponse fromEntity(Comment comment) {
		return new CommentResponse(comment.getId(), comment.getMessage(), comment.getCreatedAt(),
				comment.getUser() != null
						? new CommentUserResponse(comment.getUser().getId(), comment.getUser().getName(),
								comment.getUser().getEmail())
						: null);
	}

	public record CommentUserResponse(String id, String name, String email) {
	}
}
