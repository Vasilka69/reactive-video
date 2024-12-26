package ru.vasili4.reactive_video.data.repository.s3;

import io.minio.ObjectWriteResponse;
import ru.vasili4.reactive_video.data.model.s3.S3File;

public interface FileS3Repository {
    void makeBucket(String bucketName);
    ObjectWriteResponse createFile(S3File file, byte[] content);
}
