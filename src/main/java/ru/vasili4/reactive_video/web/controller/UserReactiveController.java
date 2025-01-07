package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.service.validators.UserValidator;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

@Tag(name = "api-user-controller", description = "Пользователи")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive/user")
public class UserReactiveController {

    private final UserService userService;

    private final UserValidator userValidator;

    @Operation(description = "Регистрация пользователя")
    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(
            @Parameter(description = "Логин и пароль пользователя", required = true) @RequestBody UserRequestDto user) {
        return userValidator.validateBeforeCreate(user).
                then(userService.register(user).map(id -> ResponseEntity.status(HttpStatus.OK).build()));
    }
}
