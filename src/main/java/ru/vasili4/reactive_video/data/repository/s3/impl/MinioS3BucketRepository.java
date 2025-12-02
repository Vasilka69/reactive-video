package ru.vasili4.reactive_video.data.repository.s3.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.vasili4.reactive_video.data.repository.s3.S3BucketRepository;
import ru.vasili4.reactive_video.exception.S3Exception;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class MinioS3BucketRepository implements S3BucketRepository {

    private final MinioClient minioClient;

    @Override
    public void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public List<String> getAllBuckets() {
        try {
            return minioClient.listBuckets()
                    .stream()
                    .map(Bucket::name)
                    .toList();
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            minioClient.removeBucket(
                    RemoveBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public boolean isBucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            throw new S3Exception(String.format("Ошибка при проверке наличия bucket \"%s\" в S3 хранилище: %s", bucketName, e.getMessage()));
        }
    }
}
