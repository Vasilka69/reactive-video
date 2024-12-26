package ru.vasili4.reactive_video.service;

import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;

import java.util.UUID;

public interface FileService {

    Mono<FileDocument> getById(UUID id);
    Mono<UUID> create(FileRequestEntity file, byte[] content);

}
