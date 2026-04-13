package org.example.team6backend.document.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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
			throw new RuntimeException("Could not initialize minio service", e);
		}
	}

	/** Upload file to MinIO */
	public void uploadFile(String fileKey, MultipartFile file) {
		try {
			minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileKey)
					.stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
		} catch (Exception e) {
			throw new RuntimeException("Failed to upload file " + fileKey, e);
		}
	}

	/** Fetch file from MinIO */
	public InputStream downloadFile(String fileKey) {
		try {
			return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileKey).build());
		} catch (Exception e) {
			throw new RuntimeException("Failed to download file " + fileKey, e);
		}
	}

	/** Delete file from MinIO */
	public void deleteFile(String fileKey) {
		try {
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileKey).build());
		} catch (Exception e) {
			throw new RuntimeException("Failed to delete file " + fileKey, e);
		}
	}

	public InputStream getFile(String fileKey) {
		try {
			return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileKey).build());
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch file " + fileKey, e);
		}
	}
}
