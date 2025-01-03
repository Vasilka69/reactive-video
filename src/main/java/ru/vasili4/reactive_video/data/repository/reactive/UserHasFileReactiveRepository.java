package ru.vasili4.reactive_video.data.repository.reactive;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserHasFileDocument;

public interface UserHasFileReactiveRepository extends ReactiveCrudRepository<UserHasFileDocument, UserHasFileDocument.UserHasFileDocumentId> {
    Flux<UserHasFileDocument> findByIdLogin(String login);
    Flux<UserHasFileDocument> findByIdFileId(String fileId);
    Mono<Void> deleteByIdFileId(String fileId);
}
