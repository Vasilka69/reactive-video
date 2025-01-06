package ru.vasili4.reactive_video.data.repository.reactive;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

public interface FileReactiveRepository extends ReactiveCrudRepository<FileDocument, String> {

    Mono<FileDocument> findByBucketAndFilePath(String bucket, String filePath);
}
