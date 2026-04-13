package org.example.team6backend.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MinioConfig {

    private final String url;
    private final String accessKey;
    private final String secretKey;

    public MinioConfig(
            @Value("${minio.url}") String url,
            @Value("{minio.access-key}") String accessKey,
            @Value("{minio.secret-key}") String secretKey
    ) {
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

	@Bean
	public MinioClient minioClient() {
		return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
	}
}
