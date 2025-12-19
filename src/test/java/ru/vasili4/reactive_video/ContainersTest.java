package ru.vasili4.reactive_video;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ContainersTest {

    @Container
    static GenericContainer<?> mongo =
            new GenericContainer<>("mongo:7")
                    .withExposedPorts(27017);

    @Test
    void test() {
        assertThat(mongo.isRunning()).isTrue();
    }
}
