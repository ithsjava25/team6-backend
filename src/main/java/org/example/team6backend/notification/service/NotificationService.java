package org.example.team6backend.notification.service;

import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.notification.entity.Notification;
import org.example.team6backend.notification.repository.NotificationRepository;
import org.example.team6backend.user.entity.AppUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(String message, AppUser user, Incident incident) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setIncident(incident);

        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotification(String userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}
