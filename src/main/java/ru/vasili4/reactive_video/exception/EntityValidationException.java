package ru.vasili4.reactive_video.exception;


public class EntityValidationException extends BaseReactiveVideoException {

    public EntityValidationException(String message) {
        super(message);
    }

    public static EntityValidationException of(String entityType, String reason) {
        return new EntityValidationException(String.format("Ошибка валидации сущности %s: %s", entityType, reason));
    }
}
