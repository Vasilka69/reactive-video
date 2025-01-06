package ru.vasili4.reactive_video.data.model.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3File {
    private FileDocument fileDocument;
    private S3FileInfo fileInfo = null;
    private byte[] content = null;

    public S3File(FileDocument fileDocument) {
        this.fileDocument = fileDocument;
    }

    public S3File(FileDocument fileDocument, byte[] content) {
        this.fileDocument = fileDocument;
        this.content = content;
    }

    public static S3File of(FileDocument fileDocument) {
        if (fileDocument == null)
            return null;
        return new S3File(fileDocument);
    }
}
