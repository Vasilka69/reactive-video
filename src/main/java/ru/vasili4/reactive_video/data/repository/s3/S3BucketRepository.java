package ru.vasili4.reactive_video.data.repository.s3;

import java.util.List;

public interface S3BucketRepository {
    void createBucket(String bucketName);
    List<String> getAllBuckets();
    void deleteBucket(String bucketName);
    boolean isBucketExists(String bucketName);
}
