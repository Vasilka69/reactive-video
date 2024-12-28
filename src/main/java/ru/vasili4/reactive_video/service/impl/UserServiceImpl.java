package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.UserMongoRepository;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.web.dto.request.UserRequestEntity;
import ru.vasili4.reactive_video.web.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMongoRepository userMongoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<String> register(UserRequestEntity dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userMongoRepository.save(new UserDocument(dto))
                .map(UserDocument::getLogin);
    }
}
