package ru.vasili4.reactive_video.service;

import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

public interface UserService {
    Mono<String> register(UserRequestDto user);
    Mono<UserDocument> findByLogin(String login);
}
