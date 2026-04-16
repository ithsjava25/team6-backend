package org.example.team6backend.notification.controller;

import org.example.team6backend.notification.dto.NotificationResponse;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/user")
	public List<NotificationResponse> getUserNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {

		String userId = userDetails.getUser().getId();

		return notificationService.getUnreadNotifications(userId).stream().map(NotificationResponse::fromEntity).toList();
	}

	@GetMapping("/user/unread-count")
	public long getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
		String userId = userDetails.getUser().getId();
		return notificationService.getUnreadCount(userId);
	}

	@PatchMapping(("/{notificationId}/read"))
	public void markAsRead(@PathVariable Long notificationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
		String userId = userDetails.getUser().getId();
		notificationService.markAsRead(notificationId, userId);
	}
}
