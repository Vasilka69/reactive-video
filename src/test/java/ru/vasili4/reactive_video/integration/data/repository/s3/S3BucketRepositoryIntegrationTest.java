package ru.vasili4.reactive_video.integration.data.repository.s3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.vasili4.reactive_video.config.TestConfig;
import ru.vasili4.reactive_video.data.repository.s3.S3BucketRepository;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestConfig.class)
@SpringBootTest
@DisplayName("Интеграционные тесты S3 Bucket репозитория")
public class S3BucketRepositoryIntegrationTest {

    @Autowired
    private S3BucketRepository s3BucketRepository;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        s3BucketRepository.createBucket(bucketName);
    }

    @AfterEach
    void tearDown() {
        s3BucketRepository.deleteBucket(bucketName);
        boolean isBucketExists = s3BucketRepository.isBucketExists(bucketName);
        assertFalse(isBucketExists);
    }

    @Test
    @DisplayName("Проверка наличия существующего bucket")
    void isBucketExists_BucketExists_ReturnsTrue() {
        // when
        boolean isBucketExists = s3BucketRepository.isBucketExists(bucketName);
        // then
        assertTrue(isBucketExists);
    }
}
