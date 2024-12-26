package ru.vasili4.reactive_video.data.model.s3;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

import java.util.UUID;

@Data
@NoArgsConstructor
public class S3File {
    private UUID fileId;
    private String bucket;
    private String filePath;

    public S3File(FileDocument fileDocument) {
        this.fileId = fileDocument.getFileId();
        this.bucket = fileDocument.getBucket();
        this.filePath = fileDocument.getFilePath();
    }

}
