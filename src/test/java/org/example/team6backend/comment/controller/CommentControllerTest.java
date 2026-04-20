package org.example.team6backend.comment.controller;

import org.example.team6backend.comment.entity.Comment;
import org.example.team6backend.comment.service.CommentService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.verify;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class,
		OAuth2ClientWebSecurityAutoConfiguration.class})
class CommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CommentService commentService;

	@Test
	void shouldReturnCommentsForIncidentId() throws Exception {
		AppUser user = new AppUser();
		user.setId("user-1");
		user.setName("Edvin");

		Incident incident = new Incident();
		incident.setId(1L);

		Comment comment = new Comment();
		comment.setMessage("Test comment");
		comment.setUser(user);
		comment.setIncident(incident);

		when(commentService.getCommentByIncidentId(1L)).thenReturn(List.of(comment));

		mockMvc.perform(get("/comments/incident/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].message").value("Test comment"));

		verify(commentService).getCommentByIncidentId(1L);
	}

	@Test
	void shouldCreateCommentAndRedirectToIncidentPage() throws Exception {
		mockMvc.perform(post("/comments").param("incidentId", "1").param("userId", "user-1").param("message", "Hello"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/incidents/1"));

		verify(commentService).createComment(1L, "user-1", "Hello");

	}
}
