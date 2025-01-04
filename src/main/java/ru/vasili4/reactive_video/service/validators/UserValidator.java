package ru.vasili4.reactive_video.service.validators;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.exception.EntityValidationException;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

@Component
@RequiredArgsConstructor
public class UserValidator {

    public void validateBeforeCreate(UserRequestDto userRequestDto) {
        validateLogin(userRequestDto);
        validatePassword(userRequestDto);
    }

    private void validateLogin(UserRequestDto userRequestDto) {
        if (StringUtils.isBlank(userRequestDto.getLogin()))
            throw EntityValidationException.of(UserDocument.ENTITY_TYPE, "Логин не должен быть пустым!");
    }

    private void validatePassword(UserRequestDto userRequestDto) {
        if (StringUtils.isBlank(userRequestDto.getPassword()))
            throw EntityValidationException.of(UserDocument.ENTITY_TYPE, "Пароль не должен быть пустым!");
        if (userRequestDto.getPassword().length() < 8)
            throw EntityValidationException.of(UserDocument.ENTITY_TYPE, "Длина пароля должна быть больше 8 символов!");
    }

}
