package ru.vasili4.reactive_video;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.vasili4.reactive_video.config.TestConfig;

@Import(TestConfig.class)
@SpringBootTest
class ReactiveVideoApplicationTest {

	@Test
	void contextLoads() {
		System.out.println("contextLoads");
	}
}
