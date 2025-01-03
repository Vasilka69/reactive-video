package ru.vasili4.reactive_video.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;

import java.util.UUID;

public interface FileService {

    Mono<FileDocument> getById(String id);
    Mono<String> create(FileRequestEntity file, String login, Mono<FilePart> filePart);
    Mono<Void> deleteById(String id);

}
