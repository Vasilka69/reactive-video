package ru.vasili4.reactive_video.client.vk.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.vasili4.reactive_video.client.WebClientWrapper;
import ru.vasili4.reactive_video.client.vk.VkOauthClient;
import ru.vasili4.reactive_video.client.vk.VkVoiceClient;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class VkVoiceClientImpl implements VkVoiceClient {

    private static final String ENCODER_PARAM_KEY = "encoder";
    private static final String ENCODER_PARAM_VALUE = "mp3";

    @Value("${vk.voice.oauth.client-id}")
    private String oauthClientId;

    @Value("${vk.voice.oauth.refresh-token}")
    private String oauthRefreshToken;

    @Value("${vk.voice.endpoint}")
    private String endpoint;

    private final VkOauthClient vkOauthClient;
    private final WebClientWrapper webClientWrapper;

    @PostConstruct
    public void init() {
        vkOauthClient.refreshToken(oauthClientId, oauthRefreshToken).subscribe();
    }

    @Override
    public Flux<DataBuffer> textToSpeech(String textToSpeech) {
        Map<String, Object> queryParams = Map.of(
                ENCODER_PARAM_KEY, ENCODER_PARAM_VALUE
        );

        return sendRequestWithAutoRefresh(
                HttpMethod.POST,
                endpoint,
                null,
                queryParams,
                null,
                Mono.just(textToSpeech),
                new ParameterizedTypeReference<>() { },
                MediaType.TEXT_PLAIN
        );
    }

    @SneakyThrows
    private <REQ, P extends Publisher<REQ>> Flux<DataBuffer> sendRequestWithAutoRefresh(
            HttpMethod method,
            String url,
            Object[] pathVariables,
            Map<String, ?> queryParams,
            MultiValueMap<String, String> headers,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType
    ) {
        return Flux.defer(() -> webClientWrapper
                        .sendRequest(
                                method,
                                url,
                                pathVariables,
                                queryParams,
                                getHeaderWithToken(headers),
                                bodyPublisher,
                                requestBodyTypeReference,
                                requestContentType
                        )
                        .bodyToFlux(DataBuffer.class))
                .retryWhen(
                        Retry.fixedDelay(5, Duration.ofSeconds(5))
                                .doBeforeRetryAsync(retrySignal -> {
                                    log.error("Ошибка отправки запроса по url = {}, method = {}, ожидание 5 секунд и обновление токена: {}", url, method, retrySignal.failure().getMessage());
                                    return vkOauthClient.refreshToken(
                                                    oauthClientId,
                                                    oauthRefreshToken
                                            )
                                            .then();
                                })
                );
    }

    private MultiValueMap<String, String> getHeaderWithToken(MultiValueMap<String, String> headers) {
        MultiValueMap<String, String> resultHeaders = headers;
        if (resultHeaders == null) {
            resultHeaders = new HttpHeaders();
        }
        resultHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(vkOauthClient.getAccessToken(oauthClientId, oauthRefreshToken)));
        return resultHeaders;
    }
}
