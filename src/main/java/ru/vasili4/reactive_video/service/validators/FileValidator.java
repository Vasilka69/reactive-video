package ru.vasili4.reactive_video.service.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.exception.EntityValidationException;

import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private final S3FileRepository s3FileRepository;

    public Mono<Void> validateBeforeCreate(FileDocument fileDocument) {
        return validateFileId(fileDocument)
                .then(validateBucket(fileDocument))
                .then(validateFilePath(fileDocument))
                .then();
    }

    private Mono<Void> validateFileId(FileDocument fileDocument) {
        return Mono.just(UUID.fromString(fileDocument.getFileId()))
                .then();
    }

    private Mono<Void> validateBucket(FileDocument fileDocument) {
        return Mono.just(s3FileRepository.isBucketExists(fileDocument.getBucket()))
                .flatMap(isBucketExists -> {
                    if (!isBucketExists)
                        return Mono.error(new EntityValidationException(String.format("Bucket \"%s\" не существует", fileDocument.getBucket())));
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> validateFilePath(FileDocument fileDocument) {
        return Mono.just(Paths.get(fileDocument.getFilePath()))
                .then();
    }
}
