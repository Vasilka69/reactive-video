package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserHasFileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.repository.reactive.FileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.reactive.UserHasFileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.service.validators.FileValidator;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final FileReactiveRepository fileReactiveRepository;
    private final S3FileRepository s3FileRepository;

    private final UserHasFileReactiveRepository userHasFileReactiveRepository;

    private final FileValidator fileValidator;

    @Override
    public Mono<FileDocument> getById(String id) {
        return fileReactiveRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<String> create(FileDocument fileDocument, Mono<FilePart> filePartMono, String login) {
        fileValidator.validateBeforeCreate(fileDocument);
        return fileReactiveRepository.save(fileDocument)
                .flatMap(fileEntity ->
                        filePartMono.flatMap(filePart ->
                                DataBufferUtils.join(filePart.content())
                                        .map(dataBuffer -> {
                                            byte[] byteContent = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(byteContent);
                                            DataBufferUtils.release(dataBuffer);
                                            s3FileRepository.createFile(new S3File(fileEntity), byteContent);
                                            return fileEntity;
                                        })
                        )
                )
                .flatMap(fileEntity -> userHasFileReactiveRepository.save(
                                new UserHasFileDocument(
                                        new UserHasFileDocument.UserHasFileDocumentId(
                                                login,
                                                fileEntity.getFileId())
                                ))
                        .thenReturn(fileEntity)
                )
                .doOnSuccess(fileEntity -> log.info("Файл успешно создан: {}", fileEntity))
                .map(FileDocument::getFileId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return fileReactiveRepository.findById(id)
                .doOnSuccess(fileReactiveRepository::delete)
                .doOnSuccess(fileDocument -> s3FileRepository.deleteFile(new S3File(fileDocument)))
                .doOnSuccess(fileDocument -> userHasFileReactiveRepository.findByIdFileId(fileDocument.getFileId()).subscribe())
                .doOnSuccess(fileDocument -> log.info("Файл с ID = {} был успешно удален", id))
                .then();

    }

    @Override
    public Flux<FileDocument> getByUserLogin(String login) {
        return userHasFileReactiveRepository.findByIdLogin(login)
                .flatMap(userHasFileDocument -> fileReactiveRepository.findById(userHasFileDocument.getId().getFileId()));
    }
}
