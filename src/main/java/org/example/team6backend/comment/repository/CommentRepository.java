package org.example.team6backend.comment.repository;

import org.example.team6backend.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

	List<Comment> findByIncidentId(Long incidentId);
}
