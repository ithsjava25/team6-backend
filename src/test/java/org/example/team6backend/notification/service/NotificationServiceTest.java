package org.example.team6backend.notification.service;

import org.example.team6backend.exception.ResourceNotFoundException;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.notification.entity.Notification;
import org.example.team6backend.notification.repository.NotificationRepository;
import org.example.team6backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldCreateNotification() {
        AppUser user = new AppUser();
        user.setId("user-1");

        Incident incident = new Incident();
        incident.setId(1L);

        notificationService.createNotification("test message", user,  incident);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldReturnUserNotifications() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(new Notification(), new Notification()));

        List<Notification> result = notificationService.getUserNotifications("user-1");

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnUnreadNotifications() {
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(new Notification()));

        List<Notification> result = notificationService.getUnreadNotifications("user-1");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnUndreadCount() {
        when(notificationRepository.countByUserIdAndReadFalse("user-1"))
                .thenReturn(3L);

        long result = notificationService.getUnreadCount("user-1");

        assertThat(result).isEqualTo(3);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        AppUser user = new AppUser();
        user.setId("user-1");

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L, "user-1");

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.markAsRead(1L, "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenWrongUserTriesToMarkAsRead() {
        AppUser user = new AppUser();
        user.setId("user-1");

        Notification notification = new Notification();
        notification.setUser(user);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() ->
                notificationService.markAsRead(1L, "user-2"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldMarkAllNotificationsAsReadForIncident() {
        Notification n1 = new Notification();
        n1.setRead(false);

        Notification n2 = new Notification();
        n2.setRead(false);

        List<Notification> notifications = List.of(n1, n2);

        when(notificationRepository.findByUserIdAndIncidentIdAndReadFalse("user-1", 1L))
                .thenReturn(notifications);

        notificationService.markNotificationAsReadForIncident("user-1", 1L);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();

        verify(notificationRepository).saveAll(notifications);
    }

    @Test
    void shouldHandleEmptyNotificationList() {
        when(notificationRepository.findByUserIdAndIncidentIdAndReadFalse("user-1", 1L))
                .thenReturn(List.of());

        notificationService.markNotificationAsReadForIncident("user-1", 1L);

        verify(notificationRepository).saveAll(List.of());
    }

}
