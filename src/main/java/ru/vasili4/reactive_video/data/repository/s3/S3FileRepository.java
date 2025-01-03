package ru.vasili4.reactive_video.data.repository.s3;

import io.minio.ObjectWriteResponse;
import ru.vasili4.reactive_video.data.model.s3.S3File;

public interface S3FileRepository {
    void makeBucket(String bucketName);
    ObjectWriteResponse createFile(S3File file, byte[] content);
    void deleteFile(S3File file);
}
