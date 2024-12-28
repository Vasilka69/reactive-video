package ru.vasili4.reactive_video.data.repository.reactive.mongo;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

@Primary
public interface FileMongoRepository extends ReactiveCrudRepository<FileDocument, String> {

}
