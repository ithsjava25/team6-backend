package org.example.team6backend.activity.controller;

import org.example.team6backend.activity.dto.ActivityLogResponse;
import org.example.team6backend.activity.entity.ActivityLog;
import org.example.team6backend.activity.service.ActivityLogService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivityLogControllerTest {

	@Mock
	private ActivityLogService activityLogService;

	@InjectMocks
	private ActivityLogController activityLogController;

	@Test
	void shouldReturnActivityLogForIncidentId() {
		Incident incident = new Incident();
		incident.setId(1L);

		AppUser user = new AppUser();
		user.setId("user-1");
		user.setName("Edvin");

		ActivityLog activityLog = new ActivityLog();
		activityLog.setAction("COMMENT_ADDED");
		activityLog.setDescription("Edvin added a comment");
		activityLog.setIncident(incident);
		activityLog.setUser(user);

		when(activityLogService.getByIncidentId(1L)).thenReturn(List.of(activityLog));

		ResponseEntity<List<ActivityLogResponse>> response = activityLogController.getActivityByIncidentId(1L);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).hasSize(1);
		verify(activityLogService).getByIncidentId(1L);
	}

}
