package ru.vasili4.reactive_video.exception;


public class UserNotFoundException extends BaseReactiveVideoException {

    public UserNotFoundException() {
        super("Пользователь не найден");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
