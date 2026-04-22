package org.example.team6backend.document.controller;

import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.document.service.MinioService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.security.CustomOAuth2UserService;
import org.example.team6backend.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.team6backend.user.entity.AppUser;

@WebMvcTest(value = DocumentController.class, excludeAutoConfiguration = {
		org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
		org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class DocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private DocumentService documentService;
	@MockitoBean
	private IncidentService incidentService;
	@MockitoBean
	private MinioService minioService;
	@MockitoBean
	private CustomOAuth2UserService customOAuth2UserService;

	@BeforeEach
	void setupSecurity() {
		AppUser appUser = new AppUser();
		appUser.setName("Test User");
		appUser.setEmail("test@test.com");

		CustomUserDetails principal = new CustomUserDetails(appUser, Map.of());

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
				principal.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	void getFile_shouldReturnDocument() throws Exception {
		Incident incident = new Incident();
		incident.setId(1L);

		Document document = new Document();
		document.setFileName("test.pdf");
		document.setContentType("application/pdf");
		document.setFileKey("abc");
		document.setIncident(incident);

		when(documentService.getByFileKey("abc")).thenReturn(Optional.of(document));

		when(incidentService.getById(eq(1L), any())).thenReturn(incident);

		when(minioService.getFile("abc")).thenReturn(new ByteArrayInputStream("hello".getBytes()));

		mockMvc.perform(get("/api/documents/abc")).andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"test.pdf\""));
	}

	@Test
	void getFile_shouldReturn404_whenMissing() throws Exception {
		when(documentService.getByFileKey("abc")).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/documents/abc")).andExpect(status().isNotFound());
	}
}
