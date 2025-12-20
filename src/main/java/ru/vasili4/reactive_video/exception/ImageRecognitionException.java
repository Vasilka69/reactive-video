package ru.vasili4.reactive_video.exception;

public class ImageRecognitionException extends BaseReactiveVideoException {

    public ImageRecognitionException(Exception cause) {
        super(cause);
    }

    public ImageRecognitionException(String message) {
        super(message);
    }

    public ImageRecognitionException(String message, Exception cause) {
        super(message, cause);
    }

    public static ImageRecognitionException withDefaultMessageTemplate(String message) {
        return new ImageRecognitionException(String.format("Ошибка при распознавании объектов изображения: %s", message));
    }

    public static ImageRecognitionException withDefaultMessageTemplate(Exception cause) {
        return new ImageRecognitionException("Ошибка при распознавании объектов изображения", cause);
    }
}
