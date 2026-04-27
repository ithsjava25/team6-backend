package org.example.team6backend.incident.controller;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.user.mapper.UserMapper;
import org.example.team6backend.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class,
		OAuth2ClientWebSecurityAutoConfiguration.class})
class IncidentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private IncidentService incidentService;

	@MockitoBean
	UserService userService;

	@MockitoBean
	UserMapper userMapper;

	@MockitoBean
	private NotificationService notificationService;

	@MockitoBean
	private AuditLogService auditLogService;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	// Create Incident as Resident//
	@Test
	@WithMockUser(roles = "RESIDENT")
	void shouldCreateIncident() throws Exception {

		Incident incident = new Incident();
		incident.setId(1L);

		when(incidentService.createIncident(any(), any(), any())).thenReturn(incident);

		mockMvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON).content("""
				{
				"subject":"Leak",
				"description":"water leak",
				"incidentCategory": "DAMAGE"
				}
				""")).andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldListAllIncidents() throws Exception {
		Incident incident1 = new Incident();
		incident1.setId(1L);

		Incident incident2 = new Incident();
		incident2.setId(2L);

		when(incidentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(incident1, incident2)));

		mockMvc.perform(get("/api/incidents/all")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldGetAssignedIncidents() throws Exception {
		when(incidentService.findByAssignedTo(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/assigned")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "RESIDENT")
	void shouldGetMyIncidents() throws Exception {
		when(incidentService.findByCreatedBy(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/my")).andExpect(status().isOk());
	}

	@Test
	void getIncidentById() throws Exception {
		AppUser user = new AppUser();
		user.setId("1");
		user.setRole(UserRole.RESIDENT);

		CustomUserDetails principal = new CustomUserDetails(user, Map.of());
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
		SecurityContextHolder.setContext(context);

		Incident incident = new Incident();
		incident.setId(1L);
		when(incidentService.getById(eq(1L), any())).thenReturn(incident);

		mockMvc.perform(get("/api/incidents/1")).andExpect(status().isOk());
	}

	@Test
	void getById_shouldBlockIfAnonymous() throws Exception {
		mockMvc.perform(get("/api/incidents/1").with(anonymous())).andExpect(status().isUnauthorized());
	}

	@Test
	void getById_shouldReturn401_whenPrincipalIsWrongType() throws Exception {
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("plainUser", null,
				List.of(() -> "ROLE_RESIDENT"));

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);

		mockMvc.perform(get("/api/incidents/1")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldAssignIncident() throws Exception {
		Incident incident = new Incident();
		incident.setId(1L);

		when(incidentService.assignIncidentToHandler(eq(1L), eq("2"), any())).thenReturn(incident);

		mockMvc.perform(patch("/api/incidents/1/assign").contentType(MediaType.APPLICATION_JSON).content("""
				{
				"handlerId" : "2"

				}
				""")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldUpdateStatus() throws Exception {

		Incident incident = new Incident();
		incident.setId(1L);

		when(incidentService.updateIncidentStatus(eq(1L), any(), any())).thenReturn(incident);

		mockMvc.perform(patch("/api/incidents/1/status").contentType(MediaType.APPLICATION_JSON).content("""
				{
				"status": "RESOLVED"
				}
				""")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldCloseIncident() throws Exception {

		Incident incident = new Incident();
		incident.setId(1L);

		when(incidentService.closeIncident(eq(1L), any())).thenReturn(incident);

		mockMvc.perform(patch("/api/incidents/1/close")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldResolveIncident() throws Exception {

		Incident incident = new Incident();
		incident.setId(1L);

		when(incidentService.resolveIncident(eq(1L), any())).thenReturn(incident);

		mockMvc.perform(patch("/api/incidents/1/resolve")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldSearchAssignedIncidents() throws Exception {
		when(incidentService.searchAssignedIncidents(any(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/assigned?search=water")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldFilterAssignedIncidentsByStatus() throws Exception {
		when(incidentService.findAssignedByStatus(any(), any(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/assigned?status=OPEN")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "HANDLER")
	void shouldSearchAndFilterAssignedIncidents() throws Exception {
		when(incidentService.searchAssignedIncidents(any(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/assigned?search=water&status=OPEN")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldSearchAllIncidents() throws Exception {
		when(incidentService.searchIncidents(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/all?search=water")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldFilterAllIncidentsByStatus() throws Exception {
		when(incidentService.findByStatus(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/incidents/all?status=OPEN")).andExpect(status().isOk());
	}
}
