package ru.vasili4.reactive_video.data.model.reactive.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Document("userHasFile")
public class UserHasFileDocument {
    @Id
    private UserHasFileDocumentId id;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserHasFileDocumentId {
        private String login;
        private UUID fileId;
    }
}
