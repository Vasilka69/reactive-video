package ru.vasili4.reactive_video.exception;

public class ResourceIllegalArgumentException extends BaseReactiveVideoException {

    public ResourceIllegalArgumentException(Exception cause) {
        super(cause);
    }

    public ResourceIllegalArgumentException(String message) {
        super(message);
    }

    public ResourceIllegalArgumentException(String message, Exception cause) {
        super(message, cause);
    }
}
