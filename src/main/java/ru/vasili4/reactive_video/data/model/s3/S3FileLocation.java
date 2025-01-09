package ru.vasili4.reactive_video.data.model.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3FileLocation {
    private String bucket;
    private String filePath;

    public S3FileLocation(FileDocument fileDocument) {
        this.bucket = fileDocument.getBucket();
        this.filePath = fileDocument.getFilePath();
    }
}
