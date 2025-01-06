package ru.vasili4.reactive_video.data.repository.s3.impl;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Repository;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.model.s3.S3FileInfo;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.exception.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RequiredArgsConstructor
@Repository
public class MinioS3FileRepository implements S3FileRepository {

    private final MinioClient minioClient;

    @Override
    public boolean isBucketExists(String bucket) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());
        } catch (Exception e) {
            throw new S3Exception(String.format("Ошибка при проверке наличия bucket \"%s\" в S3 хранилище: %s", bucket, e.getMessage()));
        }
    }

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
    public boolean isFileExists(S3File file) {
        if (file == null)
            return false;

        try {
            getFileInfo(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void uploadFile(S3File fileEntity) {
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(fileEntity.getFileDocument().getBucket())
                    .object(fileEntity.getFileDocument().getFilePath())
                    .stream(new ByteArrayInputStream(fileEntity.getContent()), -1, 1024 * 1024 * 5)
                    .build());
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public void deleteFile(S3File file) {
        if (file == null || file.getFileDocument() == null)
            return;

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(file.getFileDocument().getBucket())
                    .object(file.getFileDocument().getFilePath())
                    .build());
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public void fillFileInfo(S3File file) {
        if (file.getFileInfo() == null)
            file.setFileInfo(getFileInfo(file));
    }

    private S3FileInfo getFileInfo(S3File file) {
        StatObjectResponse stat;
        try {
            stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(file.getFileDocument().getBucket())
                            .object(file.getFileDocument().getFilePath())
                            .build());
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }

        return new S3FileInfo(stat.size());
    }

    @Override
    public S3File getFullFile(S3File file) {
        file.setContent(getFullFileContent(file));

        return file;
    }

    @Override
    public byte[] getFullFileContent(S3File file) {
        fillFileInfo(file);
        return getFileContentByRange(file, 0, file.getFileInfo().getSize());
    }

    @Override
    public byte[] safeGetFileContentByRange(S3File file, long offset, long length) {
        fillFileInfo(file);

        if (offset >= file.getFileInfo().getSize() || length == 0)
            return new byte[0];

        return getFileContentByRange(file, offset, length);
    }

    @Override
    public byte[] getFileContentByRange(S3File file, long offset, long length) {
        try (InputStream stream =
                     minioClient.getObject(
                             GetObjectArgs.builder()
                                     .bucket(file.getFileDocument().getBucket())
                                     .object(file.getFileDocument().getFilePath())
                                     .offset(offset)
                                     .length(length)
                                     .build()
                     )) {
            return IOUtils.toByteArray(stream);
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }
}
