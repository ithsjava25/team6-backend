package org.example.team6backend.document.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTest {

	@Mock
	private MinioClient minioClient;
	@InjectMocks
	private MinioService minioService;

	@Test
	void deleteFile_shouldCallMinioClient() throws Exception {
		ReflectionTestUtils.setField(minioService, "bucketName", "test");

		minioService.deleteFile("abc");

		verify(minioClient).removeObject(any(RemoveObjectArgs.class));
	}

	@Test
	void deleteFile_shouldLogWarning_whenMinioFails() throws Exception {
		ReflectionTestUtils.setField(minioService, "bucketName", "test");
		doThrow(new RuntimeException("MinIO down")).when(minioClient).removeObject(any(RemoveObjectArgs.class));
		assertDoesNotThrow(() -> minioService.deleteFile("abc"));
	}

	@Test
	void uploadFIle_shouldThrowMinioOperationException_whenMinioFails() throws Exception {
		ReflectionTestUtils.setField(minioService, "bucketName", "test");
		doThrow(new RuntimeException("MinIO down")).when(minioClient).putObject(any(PutObjectArgs.class));

		MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());

		assertThrows(MinioService.MinioOperationException.class, () -> minioService.uploadFile("key", file));
	}
}
