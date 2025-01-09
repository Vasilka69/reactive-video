package ru.vasili4.reactive_video.integration.controller;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.vasili4.reactive_video.config.TestConfig;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.data.repository.reactive.UserReactiveRepository;
import ru.vasili4.reactive_video.exception.EntityValidationException;
import ru.vasili4.reactive_video.exception.UserAlreadyExistsException;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

import static ru.vasili4.reactive_video.exception.ExceptionController.createErrorResponseDto;

@Import(TestConfig.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Интеграционные тесты контроллера пользователей")
class UserReactiveControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserReactiveRepository userReactiveRepository;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        userReactiveRepository.deleteAll().subscribe();
        userReactiveRepository.save(new UserDocument("Existent Login", "Password")).subscribe();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void register_FieldsIsValid_SuccessfulRegistration() {
        // given
        UserRequestDto user = new UserRequestDto("Login", "Password");

        // when
        webTestClient.post()
                .uri("/api/v1/reactive/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                // then
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с пустым логином")
    void register_BlankLogin_ThrowsEntityValidationException() {
        // given
        UserRequestDto user = new UserRequestDto("", "Password");
        EntityValidationException exception = new EntityValidationException("Логин не должен быть пустым!");

        // when
        webTestClient.post()
                .uri("/api/v1/reactive/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectStatus().isBadRequest()
                .expectBody().json(gson.toJson(createErrorResponseDto(exception)));
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с пустым паролем")
    void register_BlankPassword_ThrowsEntityValidationException() {
        // given
        UserRequestDto user = new UserRequestDto("Login", "");
        EntityValidationException exception = new EntityValidationException("Пароль не должен быть пустым!");

        // when
        webTestClient.post()
                .uri("/api/v1/reactive/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectStatus().isBadRequest()
                .expectBody().json(gson.toJson(createErrorResponseDto(exception)));
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с длиной пароля меньше 8 символов")
    void register_ShortPassword_ThrowsEntityValidationException() {
        // given
        UserRequestDto user = new UserRequestDto("Login", "Pass");
        EntityValidationException exception = new EntityValidationException("Длина пароля должна быть больше 8 символов!");

        // when
        webTestClient.post()
                .uri("/api/v1/reactive/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectStatus().isBadRequest()
                .expectBody().json(gson.toJson(createErrorResponseDto(exception)));
    }

    @Test
    @DisplayName("Ошибка при попытке регистрации с уже существующим логином")
    void register_LoginAlreadyExists_ThrowsUserAlreadyExistsException() {
        // given
        UserRequestDto userDto = new UserRequestDto("Existent Login", "Password");
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Пользователь с таким логином уже существует");

        // when
        webTestClient.post()
                .uri("/api/v1/reactive/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDto)
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectStatus().isBadRequest()
                .expectBody().json(gson.toJson(createErrorResponseDto(exception)));
    }
}