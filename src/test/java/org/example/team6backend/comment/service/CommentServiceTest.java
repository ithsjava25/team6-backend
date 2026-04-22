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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private IncidentRepository incidentRepository;

	@Mock
	private AppUserRepository appUserRepository;

	@Mock
	private ActivityLogService activityLogService;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private CommentService commentService;

	@Test
	void shouldReturnCommentsForIncidentId() {
		Long incidentId = 1L;

		when(commentRepository.findByIncidentId(incidentId)).thenReturn(List.of(new Comment(), new Comment()));

		List<Comment> result = commentService.getCommentByIncidentId(incidentId);

		assertThat(result).hasSize(2);
		verify(commentRepository).findByIncidentId(incidentId);
	}

	@Test
	void shouldCreateCommentWhenEverythingIsValid() {
		Long incidentId = 1L;
		String userID = "user-1";
		String message = "Test";

		Incident incident = new Incident();
		incident.setId(incidentId);

		AppUser user = new AppUser();
		user.setId(userID);
		user.setName("Edvin");

		when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
		when(appUserRepository.findById(userID)).thenReturn(Optional.of(user));
		when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

		Comment result = commentService.createComment(incidentId, userID, message);

		assertThat(result.getMessage()).isEqualTo(message);

		verify(commentRepository).save(any(Comment.class));
		verify(activityLogService).log(eq("COMMENT_ADDED"), eq("Edvin added a comment"), eq(incident), eq(user));
	}

	@Test
	void shouldThrowExceptionWhenIncidentNotFound() {
		when(incidentRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> commentService.createComment(1L, "user", "message"))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(commentRepository, never()).save(any());
	}

	@Test
	void shouldCreateNotificationWhenDifferentUserComments() {
		AppUser creator = new AppUser();
		creator.setId("user-1");

		Incident incident = new Incident();
		incident.setCreatedBy(creator);

		AppUser commenter = new AppUser();
		commenter.setId("user-2");
		commenter.setName("Edvin");

		when(incidentRepository.findById(any())).thenReturn(Optional.of(incident));
		when(appUserRepository.findById(any())).thenReturn(Optional.of(commenter));
		when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		commentService.createComment(1L, "user-2", "message");

		verify(notificationService).createNotification(contains("commented"), eq(creator), eq(incident));
	}

	@Test
	void shouldThrowExceptionWhenUserNotFound() {
		Incident incident = new Incident();
		incident.setId(1L);

		when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
		when(appUserRepository.findById("user-1")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> commentService.createComment(1L, "user-1", "message"))
				.isInstanceOf(ResourceNotFoundException.class).hasMessage("User not found");

		verify(commentRepository, never()).save(any());
		verify(activityLogService, never()).log(anyString(), anyString(), any(), any());
		verify(notificationService, never()).createNotification(anyString(), any(), any());
	}

	@Test
	void shouldNotCreateNotificationWhenUserCommentsOnOwnIncident() {
		AppUser creator = new AppUser();
		creator.setId("user-1");
		creator.setName("Edvin");

		Incident incident = new Incident();
		incident.setId(1L);
		incident.setCreatedBy(creator);

		AppUser sameUser = new AppUser();
		sameUser.setId("user-1");
		sameUser.setName("Edvin");

		when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
		when(appUserRepository.findById("user-1")).thenReturn(Optional.of(sameUser));
		when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

		commentService.createComment(1L, "user-1", "message");

		verify(notificationService, never()).createNotification(anyString(), any(), any());
	}
}
