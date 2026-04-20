//package org.example.team6backend.notification.controller;
//
//import org.example.team6backend.notification.entity.Notification;
//import org.example.team6backend.notification.service.NotificationService;
//import org.example.team6backend.security.CustomUserDetails;
//import org.example.team6backend.user.entity.AppUser;
//import org.example.team6backend.user.entity.UserRole;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
//import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
//
//@WebMvcTest(NotificationController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class,
//		OAuth2ClientWebSecurityAutoConfiguration.class})
//class NotificationControllerTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@MockitoBean
//	private NotificationService notificationService;
//
//	@AfterEach
//	void clearContext() {
//		SecurityContextHolder.clearContext();
//	}
//
//	@Test
//	void shouldReturnUnreadNotificationsForUser() throws Exception {
//		AppUser user = new AppUser();
//		user.setId("user-1");
//		user.setRole(UserRole.RESIDENT);
//
//		CustomUserDetails principal = new CustomUserDetails(user, Map.of());
//
//		Notification notification = new Notification();
//		notification.setMessage("Test notification");
//
//		when(notificationService.getUnreadNotifications("user-1")).thenReturn(List.of(notification));
//
//		SecurityContext context = SecurityContextHolder.createEmptyContext();
//		context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
//		SecurityContextHolder.setContext(context);
//
//		mockMvc.perform(get("/notifications/user")).andExpect(status().isOk())
//				.andExpect(jsonPath("$[0].message").value("Test notification"));
//
//		verify(notificationService).getUnreadCount("user-1");
//	}
//
//	@Test
//	void shouldReturnUnreadCountForUser() throws Exception {
//		AppUser user = new AppUser();
//		user.setId("user-1");
//		user.setRole(UserRole.RESIDENT);
//
//		CustomUserDetails principal = new CustomUserDetails(user, Map.of());
//
//		when(notificationService.getUnreadCount("user-1")).thenReturn(3L);
//
//		SecurityContext context = SecurityContextHolder.createEmptyContext();
//		context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
//		SecurityContextHolder.setContext(context);
//
//		mockMvc.perform(get("/notifications/user/unread-count")).andExpect(status().isOk())
//				.andExpect(content().string("3"));
//
//		verify(notificationService).getUserNotifications("user-1");
//	}
//
//	@Test
//	void shouldMarkNotificationAsRead() throws Exception {
//		AppUser user = new AppUser();
//		user.setId("user-1");
//		user.setRole(UserRole.RESIDENT);
//
//		CustomUserDetails principal = new CustomUserDetails(user, Map.of());
//
//		when(principal.getUser()).thenReturn(user);
//		when(principal.getAuthorities()).thenReturn(List.of(() -> "ROLE_RESIDENT"));
//
//		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
//				principal.getAuthorities());
//
//		mockMvc.perform(patch("/notifications/1/read").with(authentication(auth))).andExpect(status().isOk());
//
//		verify(notificationService).markAsRead(1L, "user-1");
//	}
//}
