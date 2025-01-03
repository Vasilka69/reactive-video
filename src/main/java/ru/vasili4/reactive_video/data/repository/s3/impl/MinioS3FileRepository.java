package ru.vasili4.reactive_video.data.repository.s3.impl;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.web.exception.S3Exception;

import java.io.ByteArrayInputStream;

@RequiredArgsConstructor
@Repository
public class MinioS3FileRepository implements S3FileRepository {

    private final MinioClient minioClient;

    @Override
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } catch (Exception e) {
            throw new S3Exception(e);
        }
    }

    @Override
    public ObjectWriteResponse createFile(S3File fileEntity, byte[] content) {
        try {
            return minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(fileEntity.getBucket())
                    .object(fileEntity.getFilePath())
    //				.stream(fileInputStream, Files.size(path), 1024 * 1024 * 5)
                    .stream(new ByteArrayInputStream(content), -1, 1024 * 1024 * 5)
                    .build());
        } catch (Exception e) {
            throw new S3Exception(e);
        }
    }

    @Override
    public void deleteFile(S3File fileEntity) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(fileEntity.getBucket())
                    .object(fileEntity.getFilePath())
                    .build());
        } catch (Exception e) {
            throw new S3Exception(e);
        }
    }
}
