package ru.vasili4.reactive_video.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.UserHasFileMongoRepository;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.UserMongoRepository;

@Component
@RequiredArgsConstructor
public class SecurityUserDetailsManager implements ReactiveUserDetailsService {

    private final UserMongoRepository userMongoRepository;
    private final UserHasFileMongoRepository userHasFileMongoRepository;


    @Override
    public Mono<UserDetails> findByUsername(String login) throws UsernameNotFoundException {
        if (login == null)
            return Mono.empty();

        return Mono.zip(
                        userMongoRepository.findById(login)
                                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден"))),
                        userHasFileMongoRepository.findByIdLogin(login)
                                .map(userHasFile -> new SecurityPermission(userHasFile.getId().getFileId()))
                                .collectList()
                )
                .map(tuple -> new SecurityUser(tuple.getT1(), tuple.getT1().getLogin(), tuple.getT1().getPassword(), tuple.getT2()));
    }
}
