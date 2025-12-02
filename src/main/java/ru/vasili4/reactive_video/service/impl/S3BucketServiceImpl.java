package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vasili4.reactive_video.data.repository.s3.S3BucketRepository;
import ru.vasili4.reactive_video.service.S3BucketService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3BucketServiceImpl implements S3BucketService {

    private final S3BucketRepository s3BucketRepository;

    @Override
    public void createBucket(String bucketName) {
        s3BucketRepository.createBucket(bucketName);
    }

    @Override
    public List<String> getAllBuckets() {
        return s3BucketRepository.getAllBuckets();
    }

    @Override
    public void deleteBucket(String bucketName) {
        s3BucketRepository.deleteBucket(bucketName);
    }

    @Override
    public boolean isBucketExists(String bucketName) {
        return s3BucketRepository.isBucketExists(bucketName);
    }
}
