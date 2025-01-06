package ru.vasili4.reactive_video.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vasili4.reactive_video.exception.EntityValidationException;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.service.validators.UserValidator;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера пользователей")
class UserReactiveControllerTest {

    @Mock
    UserService userService;

    @Mock
    UserValidator userValidator;

    @InjectMocks
    UserReactiveController controller;

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void register_FieldsIsValid_SuccessfulRegistration() {
        // given
        UserRequestDto user = new UserRequestDto("Login", "Password");

        doReturn(Mono.just(user.getLogin())).when(this.userService).register(user);
        doReturn(Mono.empty()).when(this.userValidator).validateBeforeCreate(user);

        // when
        StepVerifier.create(this.controller.register(user))
                // then
                .expectNext(ResponseEntity.<Void>ok(null))
                .expectComplete()
                .verify();

        verify(this.userService).register(user);
        verify(this.userValidator).validateBeforeCreate(user);
        verifyNoMoreInteractions(this.userService);
        verifyNoMoreInteractions(this.userValidator);
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с пустым логином")
    void register_BlankLogin_ThrowsEntityValidationException() {
        // given
        UserRequestDto user = new UserRequestDto("", "Password");
        EntityValidationException exception = new EntityValidationException("Логин не должен быть пустым!");

        doReturn(Mono.just(user.getLogin())).when(this.userService).register(user);
        doReturn(Mono.error(exception)).when(this.userValidator).validateBeforeCreate(user);

        // when
        StepVerifier.create(this.controller.register(user))
                // then
                .expectErrorMatches(throwable -> throwable instanceof EntityValidationException &&
                        throwable.getMessage().equals(exception.getMessage()))
                .verify();

        verify(this.userService).register(user);
        verify(this.userValidator).validateBeforeCreate(user);
        verifyNoMoreInteractions(this.userService);
        verifyNoMoreInteractions(this.userValidator);
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с пустым паролем")
    void register_BlankPassword_ThrowsEntityValidationException() {
        // given
        UserRequestDto user = new UserRequestDto("Login", "");
        EntityValidationException exception = new EntityValidationException("Пароль не должен быть пустым!");

        doReturn(Mono.just(user.getLogin())).when(this.userService).register(user);
        doReturn(Mono.error(exception)).when(this.userValidator).validateBeforeCreate(user);

        // when
        StepVerifier.create(this.controller.register(user))
                // then
                .expectErrorMatches(throwable -> throwable instanceof EntityValidationException &&
                        throwable.getMessage().equals(exception.getMessage()))
                .verify();

        verify(this.userService).register(user);
        verify(this.userValidator).validateBeforeCreate(user);
        verifyNoMoreInteractions(this.userService);
        verifyNoMoreInteractions(this.userValidator);
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с длиной пароля меньше 8 символов")
    void register_ShortPassword_ThrowsEntityValidationException() {
        // given
        UserRequestDto user = new UserRequestDto("Login", "Pass");
        EntityValidationException exception = new EntityValidationException("Длина пароля должна быть больше 8 символов!");

        doReturn(Mono.just(user.getLogin())).when(this.userService).register(user);
        doReturn(Mono.error(exception)).when(this.userValidator).validateBeforeCreate(user);

        // when
        StepVerifier.create(this.controller.register(user))
                // then
                .expectErrorMatches(throwable -> throwable instanceof EntityValidationException &&
                        throwable.getMessage().equals(exception.getMessage()))
                .verify();

        verify(this.userService).register(user);
        verify(this.userValidator).validateBeforeCreate(user);
        verifyNoMoreInteractions(this.userService);
        verifyNoMoreInteractions(this.userValidator);
    }
}