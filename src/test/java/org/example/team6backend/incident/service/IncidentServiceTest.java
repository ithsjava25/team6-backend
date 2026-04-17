package org.example.team6backend.incident.service;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.notification.service.NotificationService;
import org.example.team6backend.activity.service.ActivityLogService;
import org.example.team6backend.document.service.DocumentService;
import org.example.team6backend.document.service.MinioService;
import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.repository.IncidentRepository;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.repository.AppUserRepository;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.incident.entity.IncidentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

	@InjectMocks
	IncidentService incidentService;

	@Mock
	IncidentRepository incidentRepository;

	@Mock
	AppUserRepository userRepository;

	@Mock
	private DocumentService documentService;

	@Mock
	private MinioService minioService;

	@Mock
	private ActivityLogService activityLogService;

	@Mock
	private NotificationService notificationService;

	private Incident incident;
	private AppUser user;
	private AppUser handler;

	@BeforeEach
	void setUp() {

		user = new AppUser();
		user.setId("1");
		user.setName("Resident");
		user.setRole(UserRole.RESIDENT);

		handler = new AppUser();
		handler.setId("2");
		handler.setName("Handler");
		handler.setRole(UserRole.HANDLER);

		incident = new Incident();
		incident.setId(1L);
		incident.setCreatedBy(user);
		incident.setIncidentStatus(IncidentStatus.OPEN);
	}

	@Test
	@DisplayName("Should save created incident excluding files")
	void createIncident_shouldSaveWithoutFiles() {
		when(incidentRepository.save(any())).thenReturn(incident);

		Incident result = incidentService.createIncident(new IncidentRequest(), List.of(), user);

		assertNotNull(result);
		verify(incidentRepository).save(any());
	}

	@Test
	@DisplayName("Should save created incident including files")
	void createIncident_shouldSaveWithFiles() {
		MultipartFile file = Mockito.mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);

		Document document = new Document();
		document.setFileKey("abc123");

		when(incidentRepository.save(any())).thenReturn(incident);
		when(documentService.uploadFile(file, incident)).thenReturn(document);

		Incident result = incidentService.createIncident(new IncidentRequest(), List.of(file), user);

		assertNotNull(result);
		verify(documentService).uploadFile(file, incident);
	}

	@Test
	@DisplayName("Should return all incidents for Admin")
	void getById_shouldReturnIncidentsForAdmin() {
		user.setRole(UserRole.ADMIN);
		when(incidentRepository.findByIdWithDocuments(1L)).thenReturn(Optional.of(incident));

		Incident result = incidentService.getById(1L, user);

		assertEquals(incident, result);
	}

	@Test
	@DisplayName("Should return assigned incidents for Handler")
	void getById_shouldReturnIncidentsForHandler() {
		user.setRole(UserRole.HANDLER);
		when(incidentRepository.findByIdWithDocuments(1L)).thenReturn(Optional.of(incident));

		Incident result = incidentService.getById(1L, user);

		assertEquals(incident, result);
	}

	@Test
	@DisplayName("Should return incidents created by user")
	void getById_shouldReturnIncidentsForResident() {
		user.setRole(UserRole.RESIDENT);
		when(incidentRepository.findByIdWithDocuments(1L)).thenReturn(Optional.of(incident));

		Incident result = incidentService.getById(1L, user);

		assertEquals(incident, result);

	}

	@Test
	@DisplayName("Should block unauthorized users and throw Access Denied")
	void getById_shouldThrowIfUnauthorized() {
		user.setRole(UserRole.PENDING);

		when(incidentRepository.findByIdWithDocuments(1L)).thenReturn(Optional.of(incident));

		assertThrows(ResponseStatusException.class, () -> incidentService.getById(1L, user));
	}

	@Test
	@DisplayName("Should assign new incident to Handler")
	void assignIncident_shouldSetAssigned() {
		when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

		when(userRepository.findById("2")).thenReturn(Optional.of(handler));

		when(incidentRepository.save(any())).thenReturn(incident);

		incidentService.assignIncidentToHandler(1L, "2", user);

		assertEquals(handler, incident.getAssignedTo());
		verify(incidentRepository).save(incident);
	}

	@Test
	void updateStatus_shouldChangeStatus() {
		when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

		when(incidentRepository.save(any())).thenReturn(incident);

		when(incidentRepository.findByIdWithDocuments(1L)).thenReturn(Optional.of(incident));

		incidentService.updateIncidentStatus(1L, IncidentStatus.RESOLVED, user);

		assertEquals(IncidentStatus.RESOLVED, incident.getIncidentStatus());
	}

	@Test
	void deleteIncident() {
		when(incidentRepository.findByIdWithDocuments(1L)).thenReturn(Optional.of(incident));

		incident.setDocuments(List.of());

		incidentService.deleteIncident(1L);
		verify(incidentRepository).delete(incident);
	}
}
