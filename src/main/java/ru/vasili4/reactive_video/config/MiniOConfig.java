package ru.vasili4.reactive_video.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class MiniOConfig {

    @Value("${s3.host:http://127.0.0.1:9000}")
    private String host;

    @Value("${s3.access_key}")
    private String access_key;

    @Value("${s3.secret_key}")
    private String secret_key;

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(host)
                .credentials(access_key, secret_key)
                .build();
    }
}
