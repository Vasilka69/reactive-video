package ru.vasili4.reactive_video.service.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;

import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private final S3FileRepository s3FileRepository;

    public void validateBeforeCreate(FileDocument fileDocument) {
        validateFileId(fileDocument);
        validateBucket(fileDocument);
        validateFilePath(fileDocument);
    }

    private void validateFileId(FileDocument fileDocument) {
        UUID.fromString(fileDocument.getFileId());
    }

    private void validateBucket(FileDocument fileDocument) {
        s3FileRepository.isBucketExists(fileDocument.getBucket());
    }

    private void validateFilePath(FileDocument fileDocument) {
        Paths.get(fileDocument.getFilePath());
    }

}
