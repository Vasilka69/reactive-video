package ru.vasili4.reactive_video.exception;

public class BaseReactiveVideoException extends RuntimeException {

    public BaseReactiveVideoException(Exception cause) {
        super(cause);
    }

    public BaseReactiveVideoException(String message) {
        super(message);
    }

    public BaseReactiveVideoException(String message, Exception cause) {
        super(message, cause);
    }
}
