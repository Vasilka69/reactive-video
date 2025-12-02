package ru.vasili4.reactive_video.client.vk;

import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface WebClientWrapper {

    <REQ, P extends Publisher<REQ>, RES> Mono<RES> sendRequest(
            String url,
            Object[] pathVariables,
            Map<String, ?> queryParams,
            HttpMethod method,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType,
            ParameterizedTypeReference<RES> responseBodyTypeReference
    );
}