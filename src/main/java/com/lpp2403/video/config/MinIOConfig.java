package com.lpp2403.video.config;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {
    private static final Logger log = LoggerFactory.getLogger(MinIOConfig.class);
    @Value("${MinIO_URL}")
    private String url;

    @Value("${MinIO_ACCESS_KEY}")
    private String accessKey;

    @Value("${MinIO_SECRET_KEY}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        log.info(url);
        log.info(accessKey);
        log.info(secretKey);
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
