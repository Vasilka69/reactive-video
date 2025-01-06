package ru.vasili4.reactive_video.data.repository.s3;

import ru.vasili4.reactive_video.data.model.s3.S3File;

public interface S3FileRepository {
    boolean isBucketExists(String bucket);
    void createBucket(String bucketName);

    boolean isFileExists(S3File file);
    void uploadFile(S3File file);
    void deleteFile(S3File file);

    void fillFileInfo(S3File file);

    S3File getFullFile(S3File file);
    byte[] getFullFileContent(S3File file);
    byte[] safeGetFileContentByRange(S3File file, long offset, long length);
    byte[] getFileContentByRange(S3File file, long offset, long length);
}
