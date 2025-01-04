package ru.vasili4.reactive_video.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

public interface FileService {

    Mono<FileDocument> getById(String id);
    Mono<String> create(FileDocument file, String login, Mono<FilePart> filePart);
    Mono<Void> deleteById(String id);
    Flux<FileDocument> getByUserLogin(String login);
}
