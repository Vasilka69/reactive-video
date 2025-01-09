package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserHasFileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.model.s3.S3FileLocation;
import ru.vasili4.reactive_video.data.repository.reactive.FileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.reactive.UserHasFileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.exception.BaseReactiveVideoException;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.service.validators.FileValidator;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;
import ru.vasili4.reactive_video.utils.CustomDataBufferUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

    @Value("${file.async-load-chunk-size:#{1024 * 1024}}")
    private long asyncLoadChunkSize;

    private final FileReactiveRepository fileReactiveRepository;
    private final S3FileRepository s3FileRepository;

    private final UserHasFileReactiveRepository userHasFileReactiveRepository;

    private final FileValidator fileValidator;

    @Override
    public Mono<FileDocument> getFileMetadataById(String id) {
        return fileReactiveRepository.findById(id);
    }

    @Override
    public Mono<S3File> getS3FileWithoutContentById(String id) {
        return getFileMetadataById(id)
                .map(S3FileLocation::new)
                .map(s3FileRepository::getFileWithInfo);
    }

    @Override
    @Transactional
    public Mono<String> create(FileDocument file, Mono<FilePart> filePartMono, String login) {
        return fileValidator.validateBeforeCreate(file)
                .then(fileReactiveRepository.save(file)
                        .flatMap(fileEntity -> {
                                    if (!s3FileRepository.isBucketExists(fileEntity.getBucket()))
                                        s3FileRepository.createBucket(fileEntity.getBucket());
                                    return filePartMono.flatMap(filePart ->
                                            CustomDataBufferUtils.join(filePart.content())
                                                    .map(dataBuffer -> {
                                                        s3FileRepository.uploadFile(new S3File(
                                                                new S3FileLocation(fileEntity),
                                                                CustomDataBufferUtils.readAllBytesArray(dataBuffer)
                                                        ));
                                                        return fileEntity;
                                                    })
                                    );
                                }
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
                .then(getFileMetadataById(id)
                        .flatMap(fileEntity ->
                                filePartMono.flatMap(filePart ->
                                        CustomDataBufferUtils.join(filePart.content())
                                                .map(dataBuffer -> {
                                                    s3FileRepository.uploadFile(new S3File(
                                                            new S3FileLocation(fileEntity),
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
        return getFileMetadataById(id)
                .doOnSuccess(fileDocument -> s3FileRepository.deleteFile(new S3FileLocation(fileDocument)))
                .doOnSuccess(fileDocument -> fileReactiveRepository.deleteById(id).subscribe())
                .doOnSuccess(fileDocument -> userHasFileReactiveRepository.deleteByIdFileId(id).subscribe())
                .doOnSuccess(fileDocument -> log.info("Файл с ID = {} был успешно удален", id))
                .then();

    }

    @Override
    public Flux<FileDocument> getAllMetadataByUserLogin(String login) {
        return userHasFileReactiveRepository.findByIdLogin(login)
                .flatMap(userHasFileDocument -> getFileMetadataById(userHasFileDocument.getId().getFileId()));
    }

    @Override
    public Flux<DataBuffer> asyncGetFullFileContentById(String id) {
        return getS3FileWithoutContentById(id)
                .flux()
                .flatMap(s3File -> Flux.generate(() -> new AtomicLong(0L), (state, sink) -> {
                            long currIndex = state.getAndAdd(asyncLoadChunkSize);
                            if (ByteArrayUtils.isRangeFinished(currIndex, asyncLoadChunkSize, s3File.getFileInfo().getSize())) {
                                sink.complete();
                                return state;
                            } else {
                                try {
                                    sink.next(CustomDataBufferUtils.join(
                                            s3FileRepository.asyncGetFileBytesByRange(s3File.getS3FileLocation(),
                                                    currIndex, asyncLoadChunkSize)
                                    ).toFuture().get());
                                } catch (Exception e) {
                                    throw new BaseReactiveVideoException("При загрузке файла произошла ошибка: ", e);
                                }
                            }
                            return state;
                        })
                );
    }

    @Override
    public Flux<DataBuffer> getFullFileContentById(String id) {
        return getS3FileWithoutContentById(id)
                .flux()
                .flatMap(s3File -> Flux.generate(() -> new AtomicLong(0L), (state, sink) -> {
                            long currIndex = state.getAndAdd(asyncLoadChunkSize);
                            byte[] chunk = s3FileRepository.safeGetFileBytesByRange(s3File.getS3FileLocation(),
                                    currIndex, asyncLoadChunkSize);
                            if (chunk.length == 0) {
                                sink.complete();
                            } else {
                                sink.next(dataBufferFactory.wrap(ByteBuffer.wrap(chunk)));
                            }
                            return state;
                        })
                );
    }

    @Override
    public Mono<Byte[]> syncGetFullFileContentById(String id) {
        return getFileMetadataById(id)
                .map(fileDocument -> s3FileRepository.getFullFile(new S3FileLocation(fileDocument)))
                .map(s3File -> ByteArrayUtils.primitiveArrayToObjectArray(s3File.getContent()));
    }

    @Override
    public Flux<Byte> syncGetFileByteRangeById(String id, long offset, long length) {
        return getFileMetadataById(id)
                .map(fileDocument -> s3FileRepository.safeGetFileBytesByRange(new S3FileLocation(fileDocument),
                        offset, length))
                .map(ByteArrayUtils::primitiveArrayToObjectArray)
                .flux()
                .flatMap(Flux::fromArray);
    }
}
