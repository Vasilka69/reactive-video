package ru.vasili4.reactive_video.service.validators;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.exception.EntityValidationException;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

@Component
@RequiredArgsConstructor
public class UserValidator {

    public Mono<Void> validateBeforeCreate(UserRequestDto userRequestDto) {
        return validateLogin(userRequestDto)
                .then(validatePassword(userRequestDto))
                .then();
    }

    private Mono<Void> validateLogin(UserRequestDto userRequestDto) {
        if (StringUtils.isBlank(userRequestDto.getLogin()))
            return Mono.error(new EntityValidationException("Логин не должен быть пустым!"));
        return Mono.empty();
    }

    private Mono<Void> validatePassword(UserRequestDto userRequestDto) {
        if (StringUtils.isBlank(userRequestDto.getPassword()))
            return Mono.error(new EntityValidationException("Пароль не должен быть пустым!"));
        if (userRequestDto.getPassword().length() < 8)
            return Mono.error(new EntityValidationException("Длина пароля должна быть больше 8 символов!"));
        return Mono.empty();
    }

}
