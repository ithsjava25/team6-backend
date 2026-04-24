package org.example.team6backend.document.controller;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.document.service.MinioService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.security.CustomOAuth2UserService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.example.team6backend.user.entity.AppUser;

@WebMvcTest(value = DocumentController.class, excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class,
		OAuth2ClientWebSecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class,
		OAuth2ClientWebSecurityAutoConfiguration.class})
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

	@MockitoBean
	private AuditLogService auditLogService;

	@Test
	void getFile_shouldReturnDocument() throws Exception {
		Incident incident = new Incident();
		incident.setId(1L);

		Document document = new Document();
		document.setFileName("test.pdf");
		document.setContentType("application/pdf");
		document.setFileKey("abc");
		document.setIncident(incident);

		AppUser appUser = new AppUser();
		appUser.setName("Test User");
		appUser.setEmail("test@test.com");

		CustomUserDetails principal = new CustomUserDetails(appUser, Map.of());
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, "password",
				principal.getAuthorities());

		when(documentService.getByFileKey("abc")).thenReturn(Optional.of(document));
		when(incidentService.getById(eq(1L), any())).thenReturn(incident);
		when(documentService.downloadFile(eq("abc"), any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

		mockMvc.perform(get("/documents/abc").with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	void getFile_shouldReturn404_whenDocumentNotFound() throws Exception {
		when(documentService.getByFileKey("abc")).thenReturn(Optional.empty());

		mockMvc.perform(get("/documents/abc")).andExpect(status().isNotFound());
	}

	@Test
	void getFile_shouldReturn404_whenIncidentNotFound() throws Exception {
		Document document = new Document();
		document.setFileName("test.pdf");
		document.setContentType("application/pdf");
		document.setFileKey("abc");

		Incident incident = new Incident();
		incident.setId(1L);
		document.setIncident(incident);

		when(documentService.getByFileKey("abc")).thenReturn(Optional.of(document));
		when(incidentService.getById(eq(1L), any())).thenReturn(null);

		mockMvc.perform(get("/documents/abc")).andExpect(status().isNotFound());
	}

	@Test
	void uploadFile_shouldReturnCreated() throws Exception {
		Incident incident = new Incident();
		incident.setId(1L);

		Document document = new Document();
		document.setFileName("test.pdf");
		document.setContentType("application/pdf");
		document.setFileKey("abc");
		document.setFileSize(1024L);
		document.setIncident(incident);

		MockMultipartFile mockFile = new MockMultipartFile("files", "test.pdf", "application/pdf", "hello".getBytes());

		AppUser appUser = new AppUser();
		appUser.setId("user-1");
		appUser.setName("Test User");
		appUser.setEmail("test@test.com");
		appUser.setRole(UserRole.RESIDENT);

		CustomUserDetails principal = new CustomUserDetails(appUser, Map.of());
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, "password",
				principal.getAuthorities());

		when(incidentService.getById(anyLong(), any())).thenReturn(incident);
		when(documentService.uploadFile(any(), any(), any())).thenReturn(document);

		mockMvc.perform(multipart("/documents/upload/1").file(mockFile).with(authentication(auth)))
				.andExpect(status().isCreated());
	}
}
