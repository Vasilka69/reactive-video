package ru.vasili4.reactive_video.client.vk.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import ru.vasili4.reactive_video.client.vk.VkOauthClient;
import ru.vasili4.reactive_video.client.vk.VkVisionClient;
import ru.vasili4.reactive_video.client.vk.WebClientWrapper;
import ru.vasili4.reactive_video.client.vk.dto.DetectResponse;
import ru.vasili4.reactive_video.exception.VKClientException;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Lazy
@Slf4j
@RequiredArgsConstructor
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

    @Value("${vk.vision.endpoint.detect}")
    private String detectEndpoint;

    @Value("${vk.vision.detect-mode}")
    private String detectMode;

    private final ObjectMapper objectMapper;
    private final VkOauthClient vkOauthClient;
    private final WebClientWrapper webClientWrapper;

    private String detectModeLabels;
    private String detectMetadataTemplate;
    private String labelsJsonPath;

    @PostConstruct
    public void init() {
        detectModeLabels = "%s_labels".formatted(detectMode.equals(OBJECT2_MODE) ? OBJECT_MODE : detectMode);
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
    }

    @Override
    public Mono<DetectResponse> detect(Flux<DataBuffer> filePublisher) {
        Map<String, Object> queryParams = Map.of(
                OAUTH_TOKEN_ATTRIBUTE, vkOauthClient.getAccessToken(),
                OAUTH_PROVIDER_ATTRIBUTE, oauthProvider
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part(DETECT_META_PART_KEY, detectMetadataTemplate)
                .header("Content-Type", "application/json");
        builder.asyncPart(DETECT_FILE_PART_KEY, filePublisher, DataBuffer.class)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .filename(DETECT_FILE_PART_KEY);

        MultiValueMap<String, HttpEntity<?>> multipartData = builder.build();

        return sendRequestWithAutoRefresh(
                detectEndpoint,
                null,
                queryParams,
                HttpMethod.POST,
                Mono.just(multipartData),
                new ParameterizedTypeReference<>() {
                },
                MediaType.MULTIPART_FORM_DATA,
                new ParameterizedTypeReference<String>() {
                }
        )
                .handle((String responseJson, SynchronousSink<List<DetectResponse.Label>> sink) -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseJson);
                        JsonNode target = root.at(labelsJsonPath);
                        sink.next(objectMapper.treeToValue(target, new TypeReference<>() { }));
                    } catch (JsonProcessingException e) {
                        sink.error(new VKClientException("Ошибка распознавания изображения при помощи VK Vision", e));
                    }
                })
                .map(DetectResponse::new);
    }

    @SneakyThrows
    private <REQ, P extends Publisher<REQ>, RES> Mono<RES> sendRequestWithAutoRefresh(
            String url,
            Object[] pathVariables,
            Map<String, ?> queryParams,
            HttpMethod method,
            P bodyPublisher,
            ParameterizedTypeReference<REQ> requestBodyTypeReference,
            MediaType requestContentType,
            ParameterizedTypeReference<RES> responseBodyTypeReference
    ) {
        while (true) {
            try {
                return webClientWrapper.sendRequest(url, pathVariables, queryParams, method, bodyPublisher, requestBodyTypeReference, requestContentType, responseBodyTypeReference);
            } catch (WebClientResponseException webClientResponseException) {
                log.error("Ошибка отправки запроса по url = {}, method = {}, ожидание 5 секунд и обновление токена: ", url, method, webClientResponseException);
                Thread.sleep(5000L);
                vkOauthClient.refreshToken();
            }
        }
    }
}
