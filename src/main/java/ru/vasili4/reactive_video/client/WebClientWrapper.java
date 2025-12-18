package ru.vasili4.reactive_video.client;

import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public interface WebClientWrapper {

    <REQ, P extends Publisher<REQ>> WebClient.ResponseSpec sendRequest(
            HttpMethod method,
            String url,
            Object[] pathVariables,
            Map<String, ?> queryParams,
            MultiValueMap<String, String> headers,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType
    );
}