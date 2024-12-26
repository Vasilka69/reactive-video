package ru.vasili4.reactive_video.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.UserHasFileMongoRepository;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.UserMongoRepository;

@Component
@RequiredArgsConstructor
public class SecurityUserDetailsManager implements UserDetailsManager {

    private final UserMongoRepository userMongoRepository;
    private final UserHasFileMongoRepository userHasFileMongoRepository;

    @Override
    public void createUser(UserDetails userDetails) {

    }

    @Override
    public void updateUser(UserDetails userDetails) {

    }

    @Override
    public void deleteUser(String s) {

    }

    @Override
    public void changePassword(String s, String s1) {

    }

    @Override
    public boolean userExists(String s) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return Mono.zip(
                        userMongoRepository.findById(login)
                                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден"))),
                        userHasFileMongoRepository.findByIdLogin(login)
                                .map(userHasFile -> new SecurityPermission(userHasFile.getId().getFileId().toString()))
                                .collectList()
                )
                .map(tuple -> new SecurityUser(tuple.getT1(), tuple.getT1().getLogin(), tuple.getT1().getPassword(), tuple.getT2()))
                .block();
    }
}
