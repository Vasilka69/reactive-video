package ru.vasili4.reactive_video.client.vk.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.client.vk.VkOauthClient;
import ru.vasili4.reactive_video.client.vk.dto.RefreshTokenRequest;
import ru.vasili4.reactive_video.client.vk.dto.RefreshTokenResponse;
import ru.vasili4.reactive_video.exception.VKClientException;

import jakarta.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
@Service
public class VkOauthClientImpl implements VkOauthClient {

    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    @Value("${vk.oauth.endpoint}")
    private String oauthEndpoint;

    @Value("${vk.oauth.client-id}")
    private String oauthClientId;

    @Value("${vk.oauth.refresh-token}")
    private String oauthRefreshToken;

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    private RefreshTokenRequest refreshTokenRequest;
    private String oauthAccessToken;

    @PostConstruct
    public void init() {
        webClient = webClientBuilder.build();
        refreshTokenRequest = new RefreshTokenRequest(oauthClientId, oauthRefreshToken, REFRESH_TOKEN_GRANT_TYPE);
        refreshToken();
    }

    @Override
    public String getAccessToken() {
        return oauthAccessToken;
    }

    @SneakyThrows
    @Override
    public void refreshToken() {
        log.info("Попытка обновления токена");
        RefreshTokenResponse refreshTokenResponse;
        while (true) {
            try {
                refreshTokenResponse = webClient
                        .method(HttpMethod.POST)
                        .uri(oauthEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(refreshTokenRequest), new ParameterizedTypeReference<>() {})
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<RefreshTokenResponse>() {})
                        .block();
                if (refreshTokenResponse == null || refreshTokenResponse.getAccessToken() == null) {
                    throw new VKClientException("При попытке обновления токена не удалось извлечь токен из ответа: %s".formatted(refreshTokenResponse));
                }
                break;
            } catch (Exception e) {
                log.error("Ошибка при попытке обновления токена, ожидание 5 секунд: ", e);
                Thread.sleep(5000L);
            }
        }
        oauthAccessToken = refreshTokenResponse.getAccessToken();
        log.info("Токен успешно обновлен");
    }
}