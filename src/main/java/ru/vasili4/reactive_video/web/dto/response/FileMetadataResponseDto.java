package ru.vasili4.reactive_video.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

@Data
@Schema(description = "Метаданные файла")
public class FileMetadataResponseDto {
    @Schema(description = "Идентификатор файла")
    private String fileId;
    @Schema(description = "Bucket S3 хранилища для файла")
    private String bucket;
    @Schema(description = "Путь файла в S3 хранилище")
    private String filePath;


    public FileMetadataResponseDto(FileDocument fileDocument) {
        this.fileId = fileDocument.getFileId();
        this.bucket = fileDocument.getBucket();
        this.filePath = fileDocument.getFilePath();
    }
}
