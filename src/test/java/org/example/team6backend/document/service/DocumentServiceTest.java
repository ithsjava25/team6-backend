package org.example.team6backend.document.service;
import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.repository.DocumentRepository;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.entity.UserRole;
import org.example.team6backend.auditlog.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

	@Mock
	private MinioService minioService;

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private MultipartFile file;

	@Mock
	private AuditLogService auditLogService;

	@InjectMocks
	private DocumentService documentService;

	private AppUser testUser;
	private Incident incident;

	@BeforeEach
	void setUp() {
		incident = new Incident();
		incident.setId(1L);

		testUser = new AppUser();
		testUser.setId("user-123");
		testUser.setGithubId("test123");
		testUser.setGithubLogin("testuser");
		testUser.setName("Test User");
		testUser.setEmail("test@test.com");
		testUser.setRole(UserRole.RESIDENT);
		testUser.setActive(true);
		testUser.setCreatedAt(Instant.now());
		testUser.setUpdatedAt(Instant.now());
	}

	@Test
	void uploadFile_shouldSaveDocument_whenSuccessful() {
		when(file.getOriginalFilename()).thenReturn("test.pdf");
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getSize()).thenReturn(100L);

		Document document = new Document();
        document.setId(1L);
		document.setFileName("test.pdf");

		when(documentRepository.save(any(Document.class))).thenReturn(document);
        doNothing().when(auditLogService).log(anyString(), anyString(), any(AppUser.class), anyString(), anyString());
		Document result = documentService.uploadFile(file, incident, testUser);

		assertEquals("test.pdf", result.getFileName());
		verify(minioService).uploadFile(anyString(), eq(file));
		verify(documentRepository).save(any(Document.class));
	}

	@Test
	void uploadFile_shouldCleanUpAndThrow_ifSaveFails() {
		when(file.getOriginalFilename()).thenReturn("test.pdf");
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getSize()).thenReturn(100L);

		doNothing().when(minioService).uploadFile(anyString(), eq(file));
		when(documentRepository.save(any())).thenThrow(new RuntimeException());

		assertThrows(RuntimeException.class, () -> documentService.uploadFile(file, incident, testUser));
		ArgumentCaptor<String> uploadedKey = ArgumentCaptor.forClass(String.class);
		verify(minioService).uploadFile(uploadedKey.capture(), eq(file));
		verify(minioService).deleteFile(uploadedKey.getValue());

		verify(auditLogService, never()).log(anyString(), anyString(), any());
	}

	@Test
	void deleteFile_shouldDeleteRepoAndMinio() {
		Document document = new Document();
        document.setId(1L);
		document.setFileKey("abc");

        doNothing().when(auditLogService).log(anyString(), anyString(), any(AppUser.class), anyString(), anyString());

		documentService.deleteFile(document, testUser);

		verify(documentRepository).delete(document);
		verify(minioService).deleteFile("abc");
	}

	@Test
	void deleteFile_shouldIgnoreMinioFail() {
		Document document = new Document();
        document.setId(1L);
		document.setFileKey("abc");

		doThrow(new RuntimeException("MinIO Failure")).when(minioService).deleteFile(anyString());
		System.out.println(minioService.getClass());

        doNothing().when(auditLogService).log(anyString(), anyString(), any(AppUser.class), anyString(), anyString());

		assertDoesNotThrow(() -> documentService.deleteFile(document, testUser));
		verify(minioService).deleteFile("abc");
		verify(documentRepository).delete(document);
	}

	@Test
	void getByFileKey_shouldReturnDocument() {
		Document document = new Document();

		when(documentRepository.findByFileKey("key")).thenReturn(Optional.of(document));

		Optional<Document> result = documentService.getByFileKey("key");
		assertTrue(result.isPresent());
	}
}
