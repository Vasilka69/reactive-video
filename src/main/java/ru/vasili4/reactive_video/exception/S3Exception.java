package ru.vasili4.reactive_video.exception;

public class S3Exception extends BaseReactiveVideoException {

    public S3Exception(Exception cause) {
        super(cause);
    }

    public S3Exception(String message) {
        super(message);
    }
}
