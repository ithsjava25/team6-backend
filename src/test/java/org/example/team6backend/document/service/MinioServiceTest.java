package org.example.team6backend.document.service;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.any;
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

}
