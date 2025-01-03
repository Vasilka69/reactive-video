package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.data.repository.reactive.UserReactiveRepository;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.web.dto.request.UserRequestEntity;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserReactiveRepository userReactiveRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<String> register(UserRequestEntity dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userReactiveRepository.save(new UserDocument(dto))
                .map(UserDocument::getLogin);
    }
}
