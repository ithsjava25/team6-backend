package org.example.team6backend.notification.controller;

import org.example.team6backend.notification.dto.NotificationResponce;
import org.example.team6backend.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/user/{userId}")
	public List<NotificationResponce> getUserNotifications(@PathVariable String userId) {
		return notificationService.getUnreadNotifications(userId).stream().map(NotificationResponce::fromEntity)
				.toList();
	}

	@GetMapping("/user/{userId}/unread-count")
	public long getUnreadCount(@PathVariable String userId) {
		return notificationService.getUnreadCount(userId);
	}

	@PatchMapping(("/{notificationId}/read"))
	public void markAsRead(@PathVariable Long notificationId) {
		notificationService.markAsRead(notificationId);
	}
}
