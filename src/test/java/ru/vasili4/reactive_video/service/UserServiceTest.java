package ru.vasili4.reactive_video.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.data.repository.reactive.UserReactiveRepository;
import ru.vasili4.reactive_video.exception.UserAlreadyExistsException;
import ru.vasili4.reactive_video.service.impl.UserServiceImpl;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса пользователей")
class UserServiceTest {

    @Mock
    UserReactiveRepository userReactiveRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void register_FieldsIsValid_SuccessfulRegistration() {
        // given
        UserRequestDto userDto = new UserRequestDto("Login", "Password");

        String encodedPassword = String.format("ENCODED_%s", userDto.getPassword());
        UserDocument userDocument = new UserDocument(userDto.getLogin(), encodedPassword);

        doReturn(Mono.empty()).when(this.userReactiveRepository).findById(userDto.getLogin());
        doReturn(Mono.just(userDocument)).when(this.userReactiveRepository).save(userDocument);
        doReturn(encodedPassword).when(this.passwordEncoder).encode(userDto.getPassword());

        // when
        StepVerifier.create(this.userService.register(userDto))
                // then
                .expectNext(userDto.getLogin())
                .expectComplete()
                .verify();

        verify(this.userReactiveRepository).findById(userDto.getLogin());
        verify(this.userReactiveRepository).save(userDocument);
        verify(this.passwordEncoder).encode(userDto.getPassword());
        verifyNoMoreInteractions(this.userReactiveRepository);
        verifyNoMoreInteractions(this.passwordEncoder);
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с уже существующим логином")
    void register_LoginAlreadyExists_ThrowsUserAlreadyExistsException() {
        // given
        UserRequestDto userDto = new UserRequestDto("Login", "Password");
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Пользователь с таким логином уже существует");

        doReturn(Mono.just(new UserDocument(userDto))).when(this.userReactiveRepository).findById(userDto.getLogin());

        // when
        StepVerifier.create(this.userService.register(userDto))
                // then
                .expectErrorMatches(throwable -> throwable instanceof UserAlreadyExistsException &&
                        throwable.getMessage().equals(exception.getMessage()))
                .verify();

        verify(this.userReactiveRepository).findById(userDto.getLogin());
        verifyNoMoreInteractions(this.userReactiveRepository);
        verifyNoInteractions(this.passwordEncoder);
    }
}