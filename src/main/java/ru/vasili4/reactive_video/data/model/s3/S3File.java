package ru.vasili4.reactive_video.data.model.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3File {
    private S3FileLocation s3FileLocation;
    private S3FileInfo fileInfo = null;
    private byte[] content = null;

    public S3File(S3FileLocation s3FileLocation) {
        this.s3FileLocation = s3FileLocation;
    }

    public S3File(S3FileLocation s3FileLocation, S3FileInfo fileInfo) {
        this.s3FileLocation = s3FileLocation;
        this.fileInfo = fileInfo;
    }

    public S3File(S3FileLocation s3FileLocation, byte[] content) {
        this.s3FileLocation = s3FileLocation;
        this.content = content;
    }
}
