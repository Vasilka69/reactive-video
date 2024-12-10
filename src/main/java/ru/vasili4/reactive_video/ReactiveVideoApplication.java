package ru.vasili4.reactive_video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.vasili4.reactive_video.config.ApplicationConfig;

@SpringBootApplication
@Import(ApplicationConfig.class)
public class ReactiveVideoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveVideoApplication.class, args);
	}

}
