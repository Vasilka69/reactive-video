package ru.vasili4.reactive_video.service.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.repository.reactive.FileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.exception.EntityValidationException;

import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private final FileReactiveRepository fileReactiveRepository;
    private final S3FileRepository s3FileRepository;

    public Mono<Void> validateBeforeCreate(FileDocument fileDocument) {
        return validateFileId(fileDocument)
                .then(validateBucket(fileDocument))
                .then(validateFilePath(fileDocument))
                .then(validateFileNotExists(fileDocument))
                .then();
    }

    public Mono<Void> validateBeforeUpdate(FileDocument fileDocument) {
        return validateFileId(fileDocument)
                .then(validateBucket(fileDocument))
                .then(validateFilePath(fileDocument))
                .then(validateFileExists(fileDocument))
                .then();
    }

    public Mono<Void> validateBeforeUpdateById(String id) {
        return validateFileExistsById(id)
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
                        return Mono.error(EntityValidationException.of(
                                FileDocument.ENTITY_TYPE,
                                String.format("Bucket с названием \"%s\" не существует", fileDocument.getBucket()))
                        );
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> validateFilePath(FileDocument fileDocument) {
        return Mono.just(Paths.get(fileDocument.getFilePath()))
                .then();
    }

    private Mono<Void> validateFileNotExists(FileDocument fileDocument) {
        EntityValidationException entityValidationException = EntityValidationException.of(FileDocument.ENTITY_TYPE,
                String.format("В bucket = \"%s\" уже существует файл по пути: \"%s\"",
                        fileDocument.getBucket(),
                        fileDocument.getFilePath()));

        return fileReactiveRepository.findByBucketAndFilePath(fileDocument.getBucket(), fileDocument.getFilePath())
                .flatMap(foundDocument -> Mono.error(entityValidationException))
                .then(Mono.just(s3FileRepository.isFileExists(S3File.of(fileDocument))))
                .flatMap(isFileExists -> {
                    if (isFileExists)
                        return Mono.error(entityValidationException);
                    return Mono.empty();
                });
    }

    private Mono<Void> validateFileExists(FileDocument fileDocument) {
        EntityValidationException entityValidationException = EntityValidationException.of(FileDocument.ENTITY_TYPE,
                String.format("В bucket = \"%s\" не существует файла с filePath = \"%s\"",
                        fileDocument.getBucket(),
                        fileDocument.getFilePath()));

        return fileReactiveRepository.findByBucketAndFilePath(fileDocument.getBucket(), fileDocument.getFilePath())
                .switchIfEmpty(Mono.error(entityValidationException))
                .then(Mono.just(s3FileRepository.isFileExists(S3File.of(fileDocument))))
                .flatMap(isFileExists -> {
                    if (!isFileExists)
                        return Mono.error(entityValidationException);
                    return Mono.empty();
                });
    }

    private Mono<Void> validateFileExistsById(String id) {
        return fileReactiveRepository.findById(id)
                .switchIfEmpty(Mono.error(EntityValidationException.of(FileDocument.ENTITY_TYPE,
                        String.format("Файл с id = %s не существует", id))))
                .flatMap(fileDocument -> Mono.just(s3FileRepository.isFileExists(S3File.of(fileDocument))))
                .flatMap(isFileExists -> {
                    if (!isFileExists)
                        return Mono.error(EntityValidationException.of(FileDocument.ENTITY_TYPE,
                                String.format("Файл с id = %s не существует в S3 хранилище", id)));
                    return Mono.empty();
                });
    }
}
