package org.example.team6backend.notification.dto;

import org.example.team6backend.notification.entity.Notification;

import java.time.LocalDateTime;

public class NotificationResponce {

    private Long id;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
    private Long incidentId;

    public static NotificationResponce fromEntity(Notification notification) {
        NotificationResponce response = new NotificationResponce();
        response.setId(notification.getId());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setIncidentId(notification.getIncident().getId());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }
}
