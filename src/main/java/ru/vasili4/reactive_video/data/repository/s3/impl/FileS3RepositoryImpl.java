package ru.vasili4.reactive_video.data.repository.s3.impl;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.repository.s3.FileS3Repository;
import ru.vasili4.reactive_video.web.exception.S3Exception;

import java.io.ByteArrayInputStream;

@RequiredArgsConstructor
@Repository
public class FileS3RepositoryImpl implements FileS3Repository {

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
                    .object(fileEntity.getFilePath().replace("\\", "/"))
    //				.stream(fileInputStream, Files.size(path), 1024 * 1024 * 5)
                    .stream(new ByteArrayInputStream(content), -1, 1024 * 1024 * 5)
                    .build());
        } catch (Exception e) {
            throw new S3Exception(e);
        }
    }
}
