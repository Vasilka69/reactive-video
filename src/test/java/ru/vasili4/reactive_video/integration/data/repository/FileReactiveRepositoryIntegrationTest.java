package ru.vasili4.reactive_video.integration.data.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;
import ru.vasili4.reactive_video.data.repository.reactive.UserReactiveRepository;

import java.util.List;

@SpringBootTest
@DisplayName("Интеграционные тесты репозитория пользователей")
public class FileReactiveRepositoryIntegrationTest {

    @Autowired
    private UserReactiveRepository userReactiveRepository;

    private List<UserDocument> initUsers;

    @BeforeEach
    void setUp() {
        initUsers = List.of(
                new UserDocument("Login_1", "Password_1"),
                new UserDocument("Login_2", "Password_2"),
                new UserDocument("Login_3", "Password_3")
        );

        userReactiveRepository.saveAll(initUsers).subscribe();
    }

    @AfterEach
    void tearDown() {
        userReactiveRepository.deleteAll().subscribe();
    }

    @Test
    @DisplayName("Поиск существующего пользователя по идентификатору")
    void findById_UserExists_ReturnsUser() {
        // when
        StepVerifier.create(this.userReactiveRepository.findById(this.initUsers.get(0).getLogin()))
                // then
                .expectNext(this.initUsers.get(0))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Попытка поиска несуществующего пользователя по идентификатору")
    void findById_UserDoesNotExists_ReturnsEmptyMono() {
        // when
        StepVerifier.create(this.userReactiveRepository.findById("NonExistentLogin"))
                // then
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }
}
