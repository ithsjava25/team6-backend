package org.example.team6backend.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.team6backend.notification.dto.NotificationResponse;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/user")
	public ResponseEntity<List<NotificationResponse>> getUserNotifications(
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		String userId = userDetails.getUser().getId();
		log.info("GET /notifications/user - Fetching notifications for user: {}", userId);
		return ResponseEntity.ok(notificationService.getUnreadNotifications(userId).stream()
				.map(NotificationResponse::fromEntity).toList());
	}

	@GetMapping("/user/unread-count")
	public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
		String userId = userDetails.getUser().getId();
		log.info("GET /notifications/user/unread-count - Fetching unread count for user: {}", userId);
		return ResponseEntity.ok(notificationService.getUnreadCount(userId));
	}

	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		String userId = userDetails.getUser().getId();
		log.info("PATCH /notifications/{}/read - Marking notification as read", notificationId);
		notificationService.markAsRead(notificationId, userId);
		return ResponseEntity.ok().build();
	}
}
