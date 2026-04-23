package org.example.team6backend.notification.controller;

import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.notification.dto.NotificationResponse;
import org.example.team6backend.notification.entity.Notification;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private CustomUserDetails createPrincipal() {
        AppUser user = new AppUser();
        user.setId("user-1");
        user.setName("Edvin");
        user.setRole(UserRole.RESIDENT);

        return new CustomUserDetails(user, Map.of());
    }

    @Test
    void shouldReturnUnreadNotificationsForUser() {
        Incident incident = new Incident();
        incident.setId(1L);

        Notification notification = new Notification();
        notification.setMessage("Test notification");
        notification.setIncident(incident);

        when(notificationService.getUnreadNotifications("user-1")).thenReturn(List.of(notification));

        CustomUserDetails principal = createPrincipal();

        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().getMessage()).isEqualTo("Test notification");
        verify(notificationService).getUnreadNotifications("user-1");
    }

    @Test
    void shouldReturnUnreadCountForUser() {
        when(notificationService.getUnreadCount("user-1")).thenReturn(3L);

        CustomUserDetails principal = createPrincipal();

        ResponseEntity<Long> response = notificationController.getUnreadCount(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(3L);
        verify(notificationService).getUnreadCount("user-1");
    }

    @Test
    void shouldMarkNotificationAsRead() {
        CustomUserDetails principal = createPrincipal();

        ResponseEntity<Void> response = notificationController.markAsRead(1L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAsRead(1L, "user-1");
    }

    @Test
    void shouldReturnEmptyListWhenNoUnreadNotifications() {
        when(notificationService.getUnreadNotifications("user-1")).thenReturn(List.of());

        CustomUserDetails principal = createPrincipal();

        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(notificationService).getUnreadNotifications("user-1");
    }
}