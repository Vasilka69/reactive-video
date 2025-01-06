package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.vasili4.reactive_video.utils.ByteArrayUtils;
import ru.vasili4.reactive_video.utils.CustomDataBufferUtils;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final static long ASYNC_LOAD_CHUNK_SIZE = 1024 * 512;  // 512 kB

    private final FileReactiveRepository fileReactiveRepository;
    private final S3FileRepository s3FileRepository;

    private final UserHasFileReactiveRepository userHasFileReactiveRepository;

    private final FileValidator fileValidator;

    @Override
    public Mono<FileDocument> getFileMetadataById(String id) {
        return fileReactiveRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<String> create(FileDocument file, Mono<FilePart> filePartMono, String login) {
        return fileValidator.validateBeforeCreate(file)
                .then(fileReactiveRepository.save(file)
                        .flatMap(fileEntity ->
                                filePartMono.flatMap(filePart ->
                                        CustomDataBufferUtils.join(filePart.content())
                                                .map(dataBuffer -> {
                                                    s3FileRepository.uploadFile(new S3File(
                                                            fileEntity,
                                                            CustomDataBufferUtils.readAllBytesArray(dataBuffer)
                                                    ));
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
                        .map(FileDocument::getFileId));
    }

    @Override
    public Mono<String> updateFileContent(String id, Mono<FilePart> filePartMono) {
        return fileValidator.validateBeforeUpdateById(id)
                .then(fileReactiveRepository.findById(id)
                        .flatMap(fileEntity ->
                                filePartMono.flatMap(filePart ->
                                        CustomDataBufferUtils.join(filePart.content())
                                                .map(dataBuffer -> {
                                                    s3FileRepository.uploadFile(new S3File(
                                                            fileEntity,
                                                            CustomDataBufferUtils.readAllBytesArray(dataBuffer)
                                                    ));
                                                    return fileEntity;
                                                })
                                )
                        )
                        .doOnSuccess(fileEntity -> log.info("Содержимое файла успешно обновлено: {}", fileEntity))
                        .map(FileDocument::getFileId));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return fileReactiveRepository.findById(id)
                .doOnSuccess(fileDocument -> s3FileRepository.deleteFile(S3File.of(fileDocument)))
                .doOnSuccess(fileDocument -> fileReactiveRepository.deleteById(id).subscribe())
                .doOnSuccess(fileDocument -> userHasFileReactiveRepository.deleteByIdFileId(id).subscribe())
                .doOnSuccess(fileDocument -> log.info("Файл с ID = {} был успешно удален", id))
                .then();

    }

    @Override
    public Flux<FileDocument> getAllByUserLogin(String login) {
        return userHasFileReactiveRepository.findByIdLogin(login)
                .flatMap(userHasFileDocument -> fileReactiveRepository.findById(userHasFileDocument.getId().getFileId()));
    }

    @Override
    public Flux<Byte[]> asyncGetFullFileContentById(String id) {
        return fileReactiveRepository.findById(id)
                .map(S3File::new)
                .flux()
                .flatMap(s3File ->
                        Flux.generate(() -> new AtomicLong(0L), (state, sink) -> {
                            long currIndex = state.getAndAdd(ASYNC_LOAD_CHUNK_SIZE);
                            Byte[] chunk = ByteArrayUtils.primitiveArrayToObjectArray(
                                    s3FileRepository.safeGetFileContentByRange(s3File, currIndex, ASYNC_LOAD_CHUNK_SIZE)
                            );
                            if (chunk.length == 0) {
                                sink.complete();
                            } else {
                                sink.next(chunk);
                            }
                            return state;
                        })
                );
    }

    @Override
    public Mono<Byte[]> syncGetFullFileContentById(String id) {
        return getFileMetadataById(id)
                .map(fileDocument -> s3FileRepository.getFullFile(new S3File(fileDocument)))
                .map(s3File -> ByteArrayUtils.primitiveArrayToObjectArray(s3File.getContent()));
    }

    @Override
    public Flux<Byte> getRangeFileContentById(String id, long offset, long length) {
        return fileReactiveRepository.findById(id)
                .map(fileDocument -> s3FileRepository.getFileContentByRange(new S3File(fileDocument), offset, length))
                .map(ByteArrayUtils::primitiveArrayToObjectArray)
                .flux()
                .flatMap(Flux::fromArray);
    }
}
