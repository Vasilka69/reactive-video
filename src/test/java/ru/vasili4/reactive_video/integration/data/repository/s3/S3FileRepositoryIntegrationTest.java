package ru.vasili4.reactive_video.integration.data.repository.s3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.vasili4.reactive_video.config.TestConfig;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.model.s3.S3FileInfo;
import ru.vasili4.reactive_video.data.model.s3.S3FileLocation;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestConfig.class)
@SpringBootTest
@DisplayName("Интеграционные тесты S3 репозитория")
public class S3FileRepositoryIntegrationTest {

    @Autowired
    private S3FileRepository s3FileRepository;

    private final String bucketName = "test-bucket";

    private List<S3File> initFiles;

    @BeforeEach
    void setUp() {
        initFiles = List.of(
                new S3File(new S3FileLocation(bucketName, "1/1.txt"), new S3FileInfo((long) "Content_1".getBytes().length), "Content_1".getBytes()),
                new S3File(new S3FileLocation(bucketName, "1/2.txt"), "Content_2".getBytes()),
                new S3File(new S3FileLocation(bucketName, "2/2.txt"), new S3FileInfo((long) "Content_3".getBytes().length), "Content_3".getBytes())
        );

        s3FileRepository.createBucket(bucketName);
        for (S3File s3File:
                initFiles) {
            s3FileRepository.uploadFile(s3File);
        }
    }

    @AfterEach
    void tearDown() {
        for (S3File s3File:
             initFiles) {
            s3FileRepository.deleteFile(s3File.getS3FileLocation());
            boolean isFileExists = s3FileRepository.isFileExists(s3File.getS3FileLocation());
            assertFalse(isFileExists);
        }

        s3FileRepository.deleteBucket(bucketName);
        boolean isBucketExists = s3FileRepository.isBucketExists(bucketName);
        assertFalse(isBucketExists);
    }

    @Test
    @DisplayName("Проверка наличия существующего bucket")
    void isBucketExists_BucketExists_ReturnsTrue() {
        // when
        boolean isBucketExists = s3FileRepository.isBucketExists(bucketName);
        // then
        assertTrue(isBucketExists);
    }

    @Test
    @DisplayName("Проверка наличия существующего файла")
    void isFileExists_FileExists_ReturnsTrue() {
        // given
        S3FileLocation fileLocation = initFiles.get(0).getS3FileLocation();
        // when
        boolean isFileExists = s3FileRepository.isFileExists(fileLocation);
        // then
        assertTrue(isFileExists);
    }

    @Test
    @DisplayName("Получение всего файла по идентификатору")
    void getFullFile_FileExists_ReturnsFullFile() {
        // given
        S3File expectedFullS3File = initFiles.get(0);

        // when
        S3File fullFile = s3FileRepository.getFullFile(expectedFullS3File.getS3FileLocation());
        s3FileRepository.fillFileInfo(fullFile);

        // then
        assertEquals(expectedFullS3File, fullFile);
    }
}
