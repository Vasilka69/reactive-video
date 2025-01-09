package ru.vasili4.reactive_video.data.repository.s3;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.model.s3.S3FileInfo;
import ru.vasili4.reactive_video.data.model.s3.S3FileLocation;

public interface S3FileRepository {
    void createBucket(String bucketName);
    void deleteBucket(String bucketName);
    boolean isBucketExists(String bucketName);

    boolean isFileExists(S3FileLocation location);
    void uploadFile(S3File file);
    void deleteFile(S3FileLocation location);

    S3File getFileWithInfo(S3FileLocation location);
    void fillFileInfo(S3File file);
    S3FileInfo getFileInfo(S3FileLocation location);

    S3File getFullFile(S3FileLocation location);

    Flux<DataBuffer> asyncGetFileBytesByRange(S3FileLocation location, long offset, long length);

    byte[] getAllFileBytes(S3FileLocation location);

    byte[] safeGetFileBytesByRange(S3FileLocation location, long offset, long length);
    byte[] getFileBytesByRange(S3FileLocation location, long offset, long length);
}
