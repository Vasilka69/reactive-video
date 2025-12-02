package ru.vasili4.reactive_video.service;

import java.util.List;

public interface S3BucketService {
    void createBucket(String bucketName);
    List<String> getAllBuckets();
    void deleteBucket(String bucketName);
    boolean isBucketExists(String bucketName);
}
