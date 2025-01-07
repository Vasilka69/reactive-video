package ru.vasili4.reactive_video.data.repository.s3;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import ru.vasili4.reactive_video.data.model.s3.S3File;

public interface S3FileRepository {
    boolean isBucketExists(String bucket);
    void createBucket(String bucketName);

    boolean isFileExists(S3File file);
    void uploadFile(S3File file);
    void deleteFile(S3File file);

    void fillFileInfo(S3File file);

    Flux<DataBuffer> asyncGetFileContentByRange(S3File file, long offset, long length);

    S3File getFullFileWithoutContent(S3File file);
    byte[] getFullFileContent(S3File file);
    byte[] safeGetFileContentByRange(S3File file, long offset, long length);
    byte[] getFileContentByRange(S3File file, long offset, long length);
}
