package ru.vasili4.reactive_video.client.impl;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.DefaultSslContextSpec;
import ru.vasili4.reactive_video.client.WebClientWrapper;

import java.net.URI;
import java.util.Map;

@Slf4j
public class WebClientWrapperImpl implements WebClientWrapper {

    public static final String HTTP_PROTOCOL_PREFIX = "http://";
    public static final String HTTPS_PROTOCOL_PREFIX = "https://";

    private final WebClient webClient;

    public WebClientWrapperImpl(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(
                        DefaultSslContextSpec.forClient()
                                .configure(sslContextBuilder -> sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE))
                ));

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public <REQ, P extends Publisher<REQ>> WebClient.ResponseSpec sendRequest(
            HttpMethod method,
            String url,
            Object[] pathVariables,
            Map<String, ?> queryParams,
            MultiValueMap<String, String> headers,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType
    ) {
        return webClient
                .method(method)
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder;

                    if (url.startsWith(HTTP_PROTOCOL_PREFIX) || url.startsWith(HTTPS_PROTOCOL_PREFIX)) {
                        builder = UriComponentsBuilder.fromHttpUrl(url);
                    } else {
                        builder.path(url);
                    }
                    if (queryParams != null) {
                        queryParams.forEach(builder::queryParam);
                    }

                    URI builtUri = builder.build(pathVariables != null ? pathVariables : new Object[0]);
                    log.debug("Отправка запроса по url = {}, method = {}", builtUri, method);
                    return builtUri;
                })
                .contentType(requestContentType)
                .headers(requestHeaders -> {
                    if (headers != null) {
                        requestHeaders.putAll(headers);
                    }
                })
                .body(bodyPublisher, requestBodyTypeReference)
                .retrieve();
    }

}