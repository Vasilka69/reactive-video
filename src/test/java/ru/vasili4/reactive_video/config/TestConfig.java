package ru.vasili4.reactive_video.config;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MongoDBContainer;

public class TestConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer("mongo:7");
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MinIOContainer minIOContainer() {
        return new MinIOContainer("minio/minio:RELEASE.2024-12-18T13-15-44Z.fips");
    }

    @Primary
    @Bean
    public MinioClient getMockMinioClient(MinIOContainer minIOContainer) {
        return MinioClient.builder()
                .endpoint(minIOContainer.getS3URL())
                .credentials(minIOContainer.getUserName(), minIOContainer.getPassword())
                .build();
    }

    @Primary
    @Bean
    public MinioAsyncClient getMockMinioAsyncClient(MinIOContainer minIOContainer) {
        return MinioAsyncClient.builder()
                .endpoint(minIOContainer.getS3URL())
                .credentials(minIOContainer.getUserName(), minIOContainer.getPassword())
                .build();
    }
}
