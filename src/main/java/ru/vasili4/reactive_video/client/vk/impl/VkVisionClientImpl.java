package ru.vasili4.reactive_video.client.vk.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.vasili4.reactive_video.client.WebClientWrapper;
import ru.vasili4.reactive_video.client.vk.VkOauthClient;
import ru.vasili4.reactive_video.client.vk.VkVisionClient;
import ru.vasili4.reactive_video.client.vk.dto.DetectResponse;
import ru.vasili4.reactive_video.exception.VKClientException;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Lazy
@Slf4j
@Service
public class VkVisionClientImpl implements VkVisionClient {

    private static final String OAUTH_TOKEN_ATTRIBUTE = "oauth_token";
    private static final String OAUTH_PROVIDER_ATTRIBUTE = "oauth_provider";

    private static final String DETECT_FILE_PART_KEY = "file";
    private static final String DETECT_META_PART_KEY = "meta";
    private static final String OBJECT_MODE = "object";
    private static final String OBJECT2_MODE = "object2";

    @Value("${vk.oauth.provider}")
    private String oauthProvider;

    @Value("${vk.vision.oauth.client-id}")
    private String oauthClientId;

    @Value("${vk.vision.oauth.refresh-token}")
    private String oauthRefreshToken;

    @Value("${vk.vision.host}")
    private String host;

    @Value("${vk.vision.endpoint.detect}")
    private String endpoint;

    @Value("${vk.vision.detect-mode}")
    private String detectMode;

    private final ObjectMapper objectMapper;
    private final VkOauthClient vkOauthClient;
    private final WebClientWrapper webClientWrapper;

    private String detectMetadataTemplate;
    private String labelsJsonPath;

    public VkVisionClientImpl(
            ObjectMapper objectMapper,
            VkOauthClient vkOauthClient,
            @Qualifier("vkVisionWebClientWrapperImpl") WebClientWrapper webClientWrapper
    ) {
        this.objectMapper = objectMapper;
        this.vkOauthClient = vkOauthClient;
        this.webClientWrapper = webClientWrapper;
    }

    @PostConstruct
    public void init() {
        String detectModeLabels = "%s_labels".formatted(detectMode.equals(OBJECT2_MODE) ? OBJECT_MODE : detectMode);
        detectMetadataTemplate = """
                {
                  "mode": [
                    "%s"
                  ],
                  "images": [
                    {
                      "name": "%s"
                    }
                  ]
                }
                """.formatted(detectMode, DETECT_FILE_PART_KEY);
        labelsJsonPath = "/body/%s/0/labels".formatted(detectModeLabels);
        vkOauthClient.refreshToken(oauthClientId, oauthRefreshToken).subscribe();
    }

    @Override
    public Mono<DetectResponse> detect(Flux<DataBuffer> filePublisher) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(OAUTH_PROVIDER_ATTRIBUTE, oauthProvider);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part(DETECT_META_PART_KEY, detectMetadataTemplate)
                .header("Content-Type", "application/json");
        builder.asyncPart(DETECT_FILE_PART_KEY, filePublisher, DataBuffer.class)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .filename(DETECT_FILE_PART_KEY);

        MultiValueMap<String, HttpEntity<?>> multipartData = builder.build();

        return sendRequestWithAutoRefresh(
                HttpMethod.POST,
                endpoint,
                null,
                queryParams,
                null,
                Mono.just(multipartData),
                new ParameterizedTypeReference<>() {
                },
                MediaType.MULTIPART_FORM_DATA,
                new ParameterizedTypeReference<String>() {
                }
        )
                .map((String responseJson) -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseJson);
                        JsonNode target = root.at(labelsJsonPath);
                        return (objectMapper.treeToValue(target, new TypeReference<List<DetectResponse.Label>>() { }));
                    } catch (JsonProcessingException e) {
                        throw new VKClientException("Ошибка распознавания изображения при помощи VK Vision", e);
                    }
                })
                .map(DetectResponse::new);
    }

    @SneakyThrows
    private <REQ, P extends Publisher<REQ>, RES> Mono<RES> sendRequestWithAutoRefresh(
            HttpMethod method,
            String url,
            Object[] pathVariables,
            Map<String, Object> queryParams,
            MultiValueMap<String, String> headers,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType,
            ParameterizedTypeReference<RES> responseBodyTypeReference
    ) {
        return Mono.defer(() ->
                        webClientWrapper
                                .sendRequest(
                                        method,
                                        url,
                                        pathVariables,
                                        getQueryParamsWithToken(queryParams),
                                        headers,
                                        bodyPublisher,
                                        requestBodyTypeReference,
                                        requestContentType
                                )
                                .bodyToMono(responseBodyTypeReference))
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

    private Map<String, Object> getQueryParamsWithToken(Map<String, Object> queryParams) {
        Map<String, Object> resultQueryParams = queryParams;
        if (resultQueryParams == null) {
            resultQueryParams = new HashMap<>();
        }
        resultQueryParams.put(OAUTH_PROVIDER_ATTRIBUTE, oauthProvider);
        resultQueryParams.put(OAUTH_TOKEN_ATTRIBUTE, vkOauthClient.getAccessToken(oauthClientId, oauthRefreshToken));
        return resultQueryParams;
    }
}
