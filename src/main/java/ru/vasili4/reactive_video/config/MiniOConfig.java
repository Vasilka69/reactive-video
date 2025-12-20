package ru.vasili4.reactive_video.config;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class MiniOConfig {

    @Value("${s3.host:http://127.0.0.1:9000}")
    private String host;

    @Value("${s3.access-key:access-key}")
    private String accessKey;

    @Value("${s3.secret-key:secret-key}")
    private String secretKey;

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(host)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public MinioAsyncClient getMinioAsyncClient() {
        return MinioAsyncClient.builder()
                .endpoint(host)
                .credentials(accessKey, secretKey)
                .build();
    }
}
