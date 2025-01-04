package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.data.repository.reactive.UserReactiveRepository;
import ru.vasili4.reactive_video.exception.UserAlreadyExistsException;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.service.validators.UserValidator;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserReactiveRepository userReactiveRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;


    @Override
    public Mono<String> register(UserRequestDto dto) {
        userValidator.validateBeforeCreate(dto);
        return findByLogin(dto.getLogin())
                .flatMap(userDocument -> {
                    if (userDocument != null)
                        return Mono.error(new UserAlreadyExistsException("Пользователь с таким логином уже существует"));
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.just(dto))
                .cast(UserRequestDto.class)
                .map(userRequestDto -> {
                    userRequestDto.setPassword(passwordEncoder.encode(dto.getPassword()));
                    return userRequestDto;
                })
                .flatMap(userRequestDto -> userReactiveRepository.save(new UserDocument(dto)))
                .doOnSuccess(userDocument -> log.info("Пользователь успешно зарегистрировался: {}", userDocument))
                .map(UserDocument::getLogin);

    }

    @Override
    public Mono<UserDocument> findByLogin(String login) {
        return userReactiveRepository.findById(login);
    }
}
