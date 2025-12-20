package ru.vasili4.reactive_video.exception;

public class VKClientException extends BaseReactiveVideoException {

    public VKClientException(Exception cause) {
        super(cause);
    }

    public VKClientException(String message) {
        super(message);
    }

    public VKClientException(String message, Exception cause) {
        super(message, cause);
    }

    public static VKClientException withDefaultMessageTemplate(String message) {
        return new VKClientException(String.format("Ошибка взаимодействия с API VK Cloud: %s", message));
    }
}
