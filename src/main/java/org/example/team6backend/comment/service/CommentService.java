package org.example.team6backend.comment.service;

import org.example.team6backend.activity.service.ActivityLogService;
import org.example.team6backend.comment.entity.Comment;
import org.example.team6backend.comment.repository.CommentRepository;
import org.example.team6backend.exception.ResourceNotFoundException;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.repository.IncidentRepository;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

	private final CommentRepository commentRepository;
	private final IncidentRepository incidentRepository;
	private final AppUserRepository appUserRepository;
	private final ActivityLogService activityLogService;
	private final NotificationService notificationService;

	public CommentService(CommentRepository commentRepository, IncidentRepository incidentRepository,
			AppUserRepository appUserRepository, ActivityLogService activityLogService, NotificationService notificationService) {
		this.commentRepository = commentRepository;
		this.incidentRepository = incidentRepository;
		this.appUserRepository = appUserRepository;
		this.activityLogService = activityLogService;
		this.notificationService = notificationService;
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

		Comment savedComment = commentRepository.save(comment);

		activityLogService.log("COMMENT_ADDED", user.getName() + " added a comment", incident, user);

		if (!incident.getCreatedBy().getId().equals(user.getId())) {
			notificationService.createNotification(
					user.getName() + " commented on your incident",
					incident.getCreatedBy(),
					incident
			);
		}

		return savedComment;
	}
}
