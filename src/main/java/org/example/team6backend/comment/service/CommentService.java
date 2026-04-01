package org.example.team6backend.comment.service;

import org.example.team6backend.comment.entity.Comment;
import org.example.team6backend.comment.repository.CommentRepository;
import org.example.team6backend.exception.ResourceNotFoundException;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.repository.IncidentRepository;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IncidentRepository incidentRepository;
    private final AppUserRepository appUserRepository;

    public CommentService(CommentRepository commentRepository,
                          IncidentRepository incidentRepository,
                          AppUserRepository appUserRepository) {
        this.commentRepository = commentRepository;
        this.incidentRepository = incidentRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Comment> getCommentByIncidentId(Long incidentId) {
        return commentRepository.findByIncidentId(incidentId);
    }

    public Comment createComment(Long incidentId, String userId, String message) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setIncident(incident);
        comment.setUser(user);
        comment.setMessage(message);

        return commentRepository.save(comment);
    }
}
