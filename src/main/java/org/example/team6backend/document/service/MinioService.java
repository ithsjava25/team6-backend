package org.example.team6backend.document.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

	private final MinioClient minioClient;

	@Value("${minio.bucket}")
	private String bucketName;

	@PostConstruct
	public void init() {
		try {
			boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

			if (!exists) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
			}
		} catch (Exception e) {
			throw new MinioOperationException("Could not initialize minio service", e);
		}
	}

	/** Upload file to MinIO */
	public void uploadFile(String fileKey, MultipartFile file) {
		try {
			minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileKey)
					.stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
		} catch (Exception e) {
			throw new MinioOperationException("Failed to upload file " + fileKey, e);
		}
	}

	/** Fetch file from MinIO */
	public InputStream downloadFile(String fileKey) {
		try {
			return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileKey).build());
		} catch (ErrorResponseException e) {
			if ("NoSuchKey".equals(e.errorResponse().code())) {
				throw new FileMissingException("File not found: " + fileKey, e);
			}
			throw new MinioOperationException("Failed to download file: " + fileKey, e);
		} catch (Exception e) {
			throw new MinioOperationException("Failed to download file: " + fileKey, e);
		}
	}

	/** Delete file from MinIO */
	public void deleteFile(String fileKey) {
		try {
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileKey).build());
		} catch (Exception e) {
			log.warn("MinIO delete failed for {}: {}", fileKey, e.getMessage());
		}
	}

	public InputStream getFile(String fileKey) {
		try {
			return downloadFile(fileKey);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + fileKey);
		}
	}
	public static class FileMissingException extends RuntimeException {
		public FileMissingException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class MinioOperationException extends RuntimeException {
		public MinioOperationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
