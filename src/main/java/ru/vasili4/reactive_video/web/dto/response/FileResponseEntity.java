package ru.vasili4.reactive_video.web.dto.response;

import lombok.Data;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

@Data
public class FileResponseEntity {
    private String bucket;
    private String filePath;


    public FileResponseEntity(FileDocument fileDocument) {
        this.bucket = fileDocument.getBucket();
        this.filePath = fileDocument.getFilePath();
    }
}
