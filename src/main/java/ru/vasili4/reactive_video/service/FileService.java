package ru.vasili4.reactive_video.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;

public interface FileService {

    Mono<FileDocument> getFileMetadataById(String id);
    Mono<S3File> getS3FileWithoutContentById(String id);
    Mono<String> create(FileDocument file, Mono<FilePart> filePartMono, String login);
    Mono<String> updateFileContent(String id, Mono<FilePart> filePartMono);
    Mono<Void> deleteById(String id);
    Flux<FileDocument> getAllMetadataByUserLogin(String login);

    Flux<DataBuffer> asyncGetFullFileContentById(String id);
    Flux<DataBuffer> getFullFileContentById(String id);
    Mono<Byte[]> blockingGetFullFileContentById(String id);
    Flux<Byte> blockingGetRangeFileContentById(String id, long offset, long length);
}
