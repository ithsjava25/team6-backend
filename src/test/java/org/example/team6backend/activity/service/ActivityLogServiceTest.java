package org.example.team6backend.activity.service;

import org.example.team6backend.activity.entity.ActivityLog;
import org.example.team6backend.activity.repository.ActivityLogRepository;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    @Test
    void shouldCreateAndSaveActivityLog() {
        String action = "COMMENT_ADDED";
        String description = "Edvin added a comment";

        Incident incident = new Incident();
        incident.setId(1L);

        AppUser user = new AppUser();
        user.setId("user-1");
        user.setName("Edvin");

        ActivityLog savedLog = new ActivityLog();
        savedLog.setAction(action);
        savedLog.setDescription(description);
        savedLog.setIncident(incident);
        savedLog.setUser(user);

        when(activityLogRepository.save(any(ActivityLog.class))).thenReturn(savedLog);

        ActivityLog result = activityLogService.log(action, description, incident, user);

        assertThat(result).isEqualTo(savedLog);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(captor.capture());

        ActivityLog logToSave = captor.getValue();
        assertThat(logToSave.getAction()).isEqualTo(action);
        assertThat(logToSave.getDescription()).isEqualTo(description);
        assertThat(logToSave.getIncident()).isEqualTo(incident);
        assertThat(logToSave.getUser()).isEqualTo(user);
    }

    @Test
    void shouldReturnActivityLogsForIncidentId() {
        Long incidentId = 1L;

        List<ActivityLog> logs = List.of(new ActivityLog(), new ActivityLog());

        when(activityLogRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId))
                .thenReturn(logs);

        List<ActivityLog> result = activityLogService.getByIncidentId(incidentId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(logs);
        verify(activityLogRepository).findByIncidentIdOrderByCreatedAtDesc(incidentId);
    }
}
