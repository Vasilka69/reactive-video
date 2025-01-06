package ru.vasili4.reactive_video.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.web.dto.response.DataBufferWrapper;

public interface FileService {

    Mono<FileDocument> getFileMetadataById(String id);
    Mono<S3File> getS3FileWithoutContentById(String id);
    Mono<String> create(FileDocument file, Mono<FilePart> filePartMono, String login);
    Mono<String> updateFileContent(String id, Mono<FilePart> filePartMono);
    Mono<Void> deleteById(String id);
    Flux<FileDocument> getAllMetadataByUserLogin(String login);

    Mono<DataBufferWrapper> getNonBlockingFullFileContentById(String id);
    Mono<Byte[]> getBlockingFullFileContentById(String id);
    Flux<Byte> getRangeFileContentById(String id, long offset, long length);
}
