package ru.vasili4.reactive_video.client.vk.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.client.vk.WebClientWrapper;

import java.net.URI;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebClientWrapperImpl implements WebClientWrapper {

    public static final String HTTP_PROTOCOL_PREFIX = "http://";
    public static final String HTTPS_PROTOCOL_PREFIX = "https://";

    private final WebClient webClient;

    @Override
    public <REQ, P extends Publisher<REQ>, RES> Mono<RES> sendRequest(
            String url,
            Object[] pathVariables,
            Map<String, ?> queryParams,
            HttpMethod method,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType,
            ParameterizedTypeReference<RES> responseBodyTypeReference
    ) {
        try {
            log.debug("Отправка запроса по url = {}, method = {}", url, method);
            return webClient
                    .method(method)
                    .uri(uriBuilder -> {
                        if (url.startsWith(HTTP_PROTOCOL_PREFIX) || url.startsWith(HTTPS_PROTOCOL_PREFIX)) {
                            return URI.create(url);
                        } else {
                            uriBuilder.path(url);
                        }
                        if (queryParams != null) {
                            queryParams.forEach(uriBuilder::queryParam);
                        }
                        return uriBuilder.build(pathVariables != null ? pathVariables : new Object[0]);
                    })
                    .contentType(requestContentType)
                    .body(bodyPublisher, requestBodyTypeReference)
                    .retrieve()
                    .bodyToMono(responseBodyTypeReference);
        } catch (Exception exception) {
            log.error("Ошибка отправки запроса по url = {}, method = {}:", url, method, exception);
            throw exception;
        }
    }
}