package ru.vasili4.reactive_video.data.model.reactive.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("file")
public class FileDocument {
    public final static String ENTITY_TYPE = "File";
    public static final String CREATED_AT_FIELD = "createdAt";

    @Id
    private String fileId;
    private String bucket;
    private String filePath;
    private Instant createdAt;
}
