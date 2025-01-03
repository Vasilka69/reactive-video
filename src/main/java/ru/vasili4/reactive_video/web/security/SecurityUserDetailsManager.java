package ru.vasili4.reactive_video.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.repository.reactive.UserHasFileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.reactive.UserReactiveRepository;

@Component
@RequiredArgsConstructor
public class SecurityUserDetailsManager implements ReactiveUserDetailsService {

    private final UserReactiveRepository userReactiveRepository;
    private final UserHasFileReactiveRepository userHasFileReactiveRepository;


    @Override
    public Mono<UserDetails> findByUsername(String login) throws UsernameNotFoundException {
        if (login == null)
            return Mono.empty();

        return Mono.zip(
                        userReactiveRepository.findById(login)
                                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден"))),
                        userHasFileReactiveRepository.findByIdLogin(login)
                                .map(userHasFile -> new SecurityPermission(userHasFile.getId().getFileId()))
                                .collectList()
                )
                .map(tuple -> new SecurityUser(tuple.getT1(), tuple.getT1().getLogin(), tuple.getT1().getPassword(), tuple.getT2()));
    }
}
