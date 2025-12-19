package ru.vasili4.reactive_video.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import ru.vasili4.reactive_video.client.WebClientWrapper;
import ru.vasili4.reactive_video.client.impl.WebClientWrapperImpl;

@Import({MiniOConfig.class, SecurityConfig.class, SwaggerConfig.class})
public class ApplicationConfig {

    @Value("${vk.vision.host:}")
    private String vkVisionHost;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Primary
    @Bean
    public WebClientWrapper webClientWrapperImpl(WebClient.Builder webClientBuilder) {
        return new WebClientWrapperImpl(webClientBuilder);
    }

    @Bean("vkVisionWebClientWrapperImpl")
    public WebClientWrapper vkVisionWebClientWrapperImpl(WebClient.Builder webClientBuilder) {
        return new WebClientWrapperImpl(webClientBuilder.baseUrl(vkVisionHost));
    }

}
