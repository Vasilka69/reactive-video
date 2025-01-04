package ru.vasili4.reactive_video.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.exception.UserNotFoundException;
import ru.vasili4.reactive_video.service.UserService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final UserService userService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        return userService.findByLogin(principal.getUser().getLogin())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Ошибка получения пользователя по токену")))
                .map(user -> authentication);
    }
}
