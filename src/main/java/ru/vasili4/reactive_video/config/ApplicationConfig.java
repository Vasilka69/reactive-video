package ru.vasili4.reactive_video.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({MiniOConfig.class, WebConfig.class, SecurityConfig.class})
@RequiredArgsConstructor
public class ApplicationConfig {

}
