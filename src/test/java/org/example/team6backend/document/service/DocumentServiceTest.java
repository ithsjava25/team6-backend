package org.example.team6backend.document.service;

import org.example.team6backend.document.entity.Document;
import org.example.team6backend.document.repository.DocumentRepository;
import org.example.team6backend.incident.entity.Incident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
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
	@InjectMocks
	private DocumentService documentService;
	@Mock
	private DocumentRepository documentRepository;
	@Mock
	private MultipartFile file;
	private Incident incident;

	@BeforeEach
	void setUp() {
		incident = new Incident();
	}

	@Test
	void uploadFile_shouldSaveDocument_whenSuccessful() {
		when(file.getOriginalFilename()).thenReturn("test.pdf");
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getSize()).thenReturn(100L);

		Document document = new Document();
		document.setFileName("test.pdf");

		when(documentRepository.save(any(Document.class))).thenReturn(document);

		Document result = documentService.uploadFile(file, incident);

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

		assertThrows(RuntimeException.class, () -> documentService.uploadFile(file, incident));
		ArgumentCaptor<String> uploadedKey = ArgumentCaptor.forClass(String.class);
		verify(minioService).uploadFile(uploadedKey.capture(), eq(file));
		verify(minioService).deleteFile(uploadedKey.getValue());
	}

	@Test
	void deleteFile_shouldDeleteRepoAndMinio() {
		Document document = new Document();
		document.setFileKey("abc");

		documentService.deleteFile(document);

		verify(documentRepository).delete(document);
		verify(minioService).deleteFile("abc");
	}

	@Test
	void deleteFile_shouldIgnoreMinioFail() {
		Document document = new Document();
		document.setFileKey("abc");

		doThrow(new RuntimeException()).when(minioService).deleteFile("abc");
		assertDoesNotThrow(() -> documentService.deleteFile(document));
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
