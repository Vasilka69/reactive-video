package ru.vasili4.reactive_video.data.repository.s3.impl;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.model.s3.S3FileInfo;
import ru.vasili4.reactive_video.data.model.s3.S3FileLocation;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.exception.S3Exception;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RequiredArgsConstructor
@Repository
public class MinioS3FileRepository implements S3FileRepository {

    private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

    private final MinioClient minioClient;
    private final MinioAsyncClient minioAsyncClient;

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

    @Override
    public boolean isFileExists(S3FileLocation location) {
        if (location == null)
            return false;

        try {
            getFileInfo(location);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void uploadFile(S3File file) {
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(file.getS3FileLocation().getBucket())
                    .object(file.getS3FileLocation().getFilePath())
                    .stream(new ByteArrayInputStream(file.getContent()), -1, 1024 * 1024 * 5)
                    .build());
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public void deleteFile(S3FileLocation location) {
        if (location == null)
            return;

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(location.getBucket())
                    .object(location.getFilePath())
                    .build());
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public S3File getFileWithInfo(S3FileLocation location) {
        if (location == null)
            return null;

        return new S3File(location, getFileInfo(location));
    }

    @Override
    public void fillFileInfo(S3File file) {
        if (file.getFileInfo() == null)
            file.setFileInfo(getFileInfo(file.getS3FileLocation()));
    }

    @Override
    public S3FileInfo getFileInfo(S3FileLocation location) {
        StatObjectResponse stat;
        try {
            stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(location.getBucket())
                            .object(location.getFilePath())
                            .build());
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }

        return new S3FileInfo(stat.size());
    }

    @Override
    public S3File getFullFile(S3FileLocation location) {
        S3File s3File = new S3File(location);
        s3File.setContent(getAllFileBytes(location));

        return s3File;
    }

    @Override
    public Flux<DataBuffer> asyncGetFileBytesByRange(S3FileLocation location, long offset, long length) {
        try {
            return Mono.fromFuture(minioAsyncClient.getObject(GetObjectArgs.builder()
                            .bucket(location.getBucket())
                            .object(location.getFilePath())
                            .offset(offset)
                            .length(length)
                            .build()))
                    .flux()
                    .flatMap(getObjectResponse -> DataBufferUtils.readInputStream(
                                    () -> getObjectResponse,
                                    dataBufferFactory,
                                    Math.toIntExact(length)
                            )
                    );
        } catch (Exception e) {
            throw S3Exception.withDefaultMessageTemplate(e.getMessage());
        }
    }

    @Override
    public byte[] getAllFileBytes(S3FileLocation location) {
        return getFileBytesByRange(location, 0, getFileInfo(location).getSize());
    }

    @Override
    public byte[] safeGetFileBytesByRange(S3FileLocation location, long offset, long length) {
        if (ByteArrayUtils.isRangeFinished(offset, length, getFileInfo(location).getSize()))
            return new byte[0];

        return getFileBytesByRange(location, offset, length);
    }

    @Override
    public byte[] getFileBytesByRange(S3FileLocation location, long offset, long length) {
        try (InputStream stream =
                     minioClient.getObject(
                             GetObjectArgs.builder()
                                     .bucket(location.getBucket())
                                     .object(location.getFilePath())
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
