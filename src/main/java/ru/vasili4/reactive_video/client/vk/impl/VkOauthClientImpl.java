package ru.vasili4.reactive_video.client.vk.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.vasili4.reactive_video.client.WebClientWrapper;
import ru.vasili4.reactive_video.client.vk.VkOauthClient;
import ru.vasili4.reactive_video.client.vk.dto.RefreshTokenRequest;
import ru.vasili4.reactive_video.client.vk.dto.RefreshTokenResponse;
import ru.vasili4.reactive_video.exception.VKClientException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class VkOauthClientImpl implements VkOauthClient {

    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    @Value("${vk.oauth.endpoint}")
    private String oauthEndpoint;

    private final WebClientWrapper webClientWrapper;

    private final Map<String, String> oauthAccessTokens = new HashMap<>();

    @Override
    public String getAccessToken(String oauthClientId) {
        return oauthAccessTokens.get(oauthClientId);
    }

    @Override
    public String getAccessToken(String oauthClientId, String oauthRefreshToken) {
        return oauthAccessTokens.computeIfAbsent(oauthClientId, (String key) -> refreshToken(key, oauthRefreshToken).block());
    }

    @SneakyThrows
    @Override
    public Mono<String> refreshToken(String oauthClientId, String oauthRefreshToken) {
        log.info("Попытка обновления токена");

        RefreshTokenRequest request =
                new RefreshTokenRequest(oauthClientId, oauthRefreshToken, REFRESH_TOKEN_GRANT_TYPE);

        return webClientWrapper.sendRequest(
                        HttpMethod.POST,
                        oauthEndpoint,
                        null,
                        null,
                        null,
                        Mono.just(request),
                        new ParameterizedTypeReference<>() { },
                        MediaType.APPLICATION_JSON
                )
                .bodyToMono(RefreshTokenResponse.class)
                .<String>handle((response, sink) -> {
                    if (response == null || response.getAccessToken() == null) {
                        sink.error(new VKClientException("Не удалось получить access token из ответа: %s".formatted(response)));
                        return;
                    }
                    sink.next(response.getAccessToken());
                })
                .doOnNext(token -> {
                    oauthAccessTokens.put(oauthClientId, token);
                    log.info("Токен успешно обновлён");
                })
                .retryWhen(
                        Retry.fixedDelay(5, Duration.ofSeconds(5))
                                .doBeforeRetry(rs ->
                                        log.error("Ошибка обновления токена, повтор через 5 сек",
                                                rs.failure())
                                )
                );
    }
}