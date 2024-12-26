package ru.vasili4.reactive_video.data.repository.reactive.mongo;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserHasFileDocument;

@Primary
public interface UserHasFileMongoRepository extends ReactiveCrudRepository<UserHasFileDocument, UserHasFileDocument.UserHasFileDocumentId> {
    Flux<UserHasFileDocument> findByIdLogin(String login);
}
