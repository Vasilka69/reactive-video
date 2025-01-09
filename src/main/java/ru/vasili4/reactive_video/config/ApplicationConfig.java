package ru.vasili4.reactive_video.config;

import org.springframework.context.annotation.Import;

@Import({MiniOConfig.class, SecurityConfig.class, SwaggerConfig.class})
public class ApplicationConfig {

}
