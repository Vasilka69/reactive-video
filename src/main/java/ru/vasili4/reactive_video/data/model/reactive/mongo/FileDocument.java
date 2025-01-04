package ru.vasili4.reactive_video.data.model.reactive.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document("file")
public class FileDocument {
    @Id
    private String fileId;
    private String bucket;
    private String filePath;
}
