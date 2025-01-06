package ru.vasili4.reactive_video.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

public interface FileService {

    Mono<FileDocument> getFileMetadataById(String id);
    Mono<String> create(FileDocument file, Mono<FilePart> filePartMono, String login);
    Mono<String> updateFileContent(String id, Mono<FilePart> filePartMono);
    Mono<Void> deleteById(String id);
    Flux<FileDocument> getAllByUserLogin(String login);
    Flux<Byte[]> asyncGetFullFileContentById(String id);
    Mono<Byte[]> syncGetFullFileContentById(String id);
    Flux<Byte> getRangeFileContentById(String id, long offset, long length);
}
